package wowPvPAnalysis

import scala.util.control.Breaks._
import scala.sys.process._
import scala.io.StdIn.readLine
import java.io.PrintWriter
import spray.json._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import com.typesafe.scalalogging.LazyLogging

class Utilities ()

object Utilities extends LazyLogging {

    /** Simple function called when user's input is invalid.
      */
    def invalid() {
        println("Invalid input! Try again.")
        waitforUser()
    }

    /** Simple function used to check if a String can be converted into an Int
      * (Just an excuse to try out Option-> Some/None)
      * @param s The input String
      * @return Some(Int) if conversion is valid, or None if invalid
      */
    def toInt(s: String): Option[Int] = {
        try {
            Some(s.toInt)
        } catch {
            case e: Exception => {
                logger.debug("Failed to parse to an Int!")
                None
            }
        }
    }

    /**
      * Simple function that causes program to pause while user looks at results
      */
    def waitforUser() {
        println("\nPress enter to resume")
        readLine()
    }

    /** Function used to start the application.
      * - Creates three futures as the application starts that run fetchData() to call the Wow PvP Leadersboards API and grab data from them to be used later.
      * - Once the introductory statements have executed and the futures have been returned, the application begins running with the beginAnalysis() method.
      */
    def start() {
        // Referenced https://users.scala-lang.org/t/executing-an-action-after-everything-in-a-list-of-futures-has-completed/1607/2
        // for the following Future implementation:
        val twos = Future {fetchData("2v2"); 1} // Fetches 2v2 data from API
        val threes = Future {fetchData("3v3"); 2} // Fetches 3v3 data from API
        val rbgs = Future {fetchData("rbg"); 3} // Fetches rbg data from API

        // Yields a result when all futures have finished
        val result = for { 
            r1 <- twos
            r2 <- threes 
            r3 <- rbgs
        } yield (r1 + r2 + r3) 

        println("\nWelcome to Cody Piazza's project zero!") // Welcome statement
        Thread.sleep(2000) // Gives user time to see welcome statement
        println("\nThis project will analyze the current World of Warcraft PvP season.\n") // States purpose of program
        Thread.sleep(2000) // Gives user time to see above message
        scala.concurrent.Await.ready(result, 10.seconds) // Wait for futures to return with our json files

        //Logs results of futures
        result.onComplete {
            case Success(number) => logger.info("Futures returned successfully!")
            case Failure(exception) => {
                logger.error("Failure! Data took too long to be retrieved!")
                exception.printStackTrace
            }
        }
        beginAnalysis() // Jump to next method
    }

    /** This method is called at the very beginning of the start() method, 
      * and is used to gather information from the Blizzard API
      * - Fetches an access token from the Blizzard API, then grabs data from the WoW PvP leadersboards Blizzard API for the current season (season 30).
      * - Converts the returned information into a JSON format and then writes it to a JSON file
      * @param bracket The relevant bracket (2v2, 3v3, rbg) that the user has selected, which will also be the file name
      * @return Returns a String Iterator containing the first 39 lines of JSON formatted API response. For testing purposes.
      */
    def fetchData(bracket: String): Iterator[String] = {
        val tokenRequest = s"curl -s -u ${sys.env.get("ID").get}:${sys.env.get("SECRET").get} -d grant_type=client_credentials https://us.battle.net/oauth/token" // Token request.
        val token = tokenRequest.!!.split("\"")(3) // Sends out token request and grabs returned result, splitting it into an array and grabbing the fourth item, which is the token.
        val url = s"https://us.api.blizzard.com/data/wow/pvp-season/30/pvp-leaderboard/$bracket?namespace=dynamic-us&locale=en_US&access_token=$token" // Call the relevant API.
        val writer = new PrintWriter(s"$bracket.json") // Writes the information into a JSON file that will be searched through later.
        val apiInfo = scala.io.Source.fromURL(url).mkString.parseJson.prettyPrint
        writer.print(apiInfo)
        writer.close()
        val testLines = scala.io.Source.fromString(apiInfo).getLines().take(39) // For testing purposes
        testLines
    }

    /** Checks if the user's input is a valid attempt at getting a quick analysis.
      * - If an attempt was detected, the information returned by this function will be used in the quickAnalysis function.
      * - Only checks for partial validity, as partial validity implies that a user attempted to use the quick analysis feature.
      * - Later, in the quickAnalysis function, user's input will be checked for full validity.
      * @param s The user's input String.
      * @return Returns a tuple in the following format: (Boolean, String, Int, String)
      * - Boolean -> Will be true if it is determined that a user attempted to run a quick analysis.
      * - String -> Will contains the user's input bracket, or "" if no bracket was detected or bracket was input incorrectly.
      * - Int -> Will contain the user's input player count, or 0 if no player count was detected or player count was input incorrectly.
      * - String -> Will contain the user's input analysis type, or "" if no analysis type was detected or analysis type was input incorrectly.
      */
    def checkValid(s: String): (Boolean, String, Int, String) = {
        var partiallyValid = false
        var bracket = ""
        var playerCount = 0
        var aType = ""
        var validityCount = 0
        s match {
            case s if s.contains("2v2 ") => bracket = "2v2"
            case s if s.contains("3v3 ") => bracket = "3v3"
            case s if s.contains("rbg ") => bracket = "rbg"
            case _ => 
        }
        s match {
            case s if s.contains(" f") => aType = "f"
            case s if s.contains(" wl") => aType = "wl"
            case s if s.contains(" r") => aType = "r"
            case _ =>
        }
        val findNum = raw"\b(\d+)\b".r.findFirstIn(s) //https://stackoverflow.com/questions/28368040/what-is-the-regex-for-matching-any-numbers-with-spaces-between-them
        if (findNum != None && findNum.get.toInt > 0 && findNum.get.toInt < 4501) playerCount = findNum.get.toInt

        if (bracket != "") validityCount +=1
        if (playerCount != 0) validityCount += 1
        if (aType != "") validityCount += 1

        if (validityCount > 1) partiallyValid = true

        (partiallyValid, bracket, playerCount, aType)
    }

    /** Checks to see if all user inputs are valid for a quick analysis.
      * - If they are, then the relevant data is grabbed and the specified analysis is performed.
      * - If not, a message is displayed displaying the correct input format for a quick analysis.
      * @param bracket The PvP bracket to analyze (2v2, 3v3, rbg).
      * @param count The number of top players to analyze in the bracket (1-4500).
      * @param analysisType Which analysis to run (f-> faction ratio, r-> realm count, wl -> win/loss ratio).
      * @return Returns a String denoting if quick analysis syntax was correct. For testing purposes.
      */
    def quickAnalysis(bracket: String, count: Int, analysisType: String): String = {
        var outputMessage = "Valid syntax"
        if (bracket == "" || count == 0 || analysisType == "") {
            outputMessage = "Input invalid! Are you attempting to do a quick analysis?\nQuick analysis format -> 2v2|3v3|rbg 1-4500 f|wl|r\nExample: 2v2 500 f"
            println(outputMessage)
            logger.info("Caught user attempting to input quick analysis, but format was wrong.") // Logs the bad attempt to run a quick analysis
        }
        else {
            val players = Player.getPlayerInfo(count, bracket)
            analysisType match {
                case "f"  => Player.hordeVsAlliance(players, bracket)
                case "r"  => Player.countRealms(players, bracket)
                case "wl" => Player.winLossRatio(players, bracket)
            }
        }
        waitforUser()
        outputMessage
    }

    /** Program asks for uses input so information can begin to be gathered about user's desired analysis.
      * - This method will be looped through continuously unless user inputs "q" to exit the application, 
      * or the user inputs a bracket name, which will call the next relevant function.
      * - User may also now perform a quick analysis, which will return results to the user, and then return back into this function
      */
    def beginAnalysis() {
        breakable { while (true) {
            println("If you wish to skip normal program navigation and perform a quick analysis, you may do so at any time")
            println("The format for a quick analysis is: 2v2|3v3|rbg 1-4500 f|wl|r\nExample: 2v2 500 f\n")
            println("Otherwise, please input one of the following options to begin:\n")
            println("2v2: Analyze the current US 2v2 arena bracket")
            println("3v3: Analyze the current US 3v3 arena bracket")
            println("rbg: Analyze the current US rated battleground season")
            println("q: Exit application\n")
            val desiredCount = readLine()
            println()
            try {
                desiredCount match {
                    case "2v2" => pvpAnalysis("2v2")
                    case "3v3" => pvpAnalysis("3v3")
                    case "rbg" => pvpAnalysis("rbg")
                    case "q" => {println("\nExiting...\n"); break}
                    case desiredCount if checkValid(desiredCount)._1 == true => {
                        val results = checkValid(desiredCount)
                        quickAnalysis(results._2, results._3, results._4)
                    }
                    case _ => invalid
                }
            } catch {
                case n : NumberFormatException => invalid
            }
        } }
    }

    /** Function runs when user selects their bracket to analyze.
      * - This method will be looped through continuously unless user inputs "q" to exit this layer of the application, 
      * or the user inputs a valid player count (1-4500), which will call the next relevant function.
      * - Makes API call to Blizzard's PvP Leaderboards API and saves it to local JSON file.
      * - Calls function "getPlayerInfo" to grab relevant player information from file and
      * generates an array containing all relevant player data in the form of "Player" objects.
      * - Calls function "analyze" to perform some basic analysis on the data within the array of "Player" objects.
      * - User may also now perform a quick analysis if they desire a different analysis than the one they are currently selecting, 
      * which will return results to the user, and then return back into this function.
      * @param bracket The pvp bracket to analyze (2v2, 3v3, rbg).
      */
    def pvpAnalysis(bracket: String) {
        var input = "" 
        while (input != "q") {
            println(s"Enter the number of top players you wish to analyze in the $bracket bracket (minimum 1, maximum 4500) to continue:")
            println(s"Enter q to return to the previous menu\n")

            input = readLine()
            println()
            val players = toInt(input)
            try {
                if (players != None && players.get > 0 && players.get < 4501) { 
                    val playerInfo = Player.getPlayerInfo(players.get, bracket)
                    analyze(playerInfo, bracket)
                }
                else if (input == "q") println(s"\nExiting $bracket analysis...")
                else if (checkValid(input)._1 == true) {
                    val results = checkValid(input)
                    quickAnalysis(results._2, results._3, results._4)
                }
                else invalid
            } catch {
                case n: NumberFormatException => invalid
            }
        }
    }

    /** Performs final analysis of user specified bracket and player count
      * - This is where the user inputs their desired analysis type
      * - This method will be looped through continuously unless user inputs "q" to exit this layer of the application.
      * - User may also now perform a quick analysis if they desire a different analysis than the one they are currently selecting,
      *  which will return results to the user, and then return back into this function.
      * @param players Array of Player objects that will be used as a total count of players, as well as arguments for functions.
      * @param bracket PvP bracket previously specified by user to be used as argument for functions.
      */
    def analyze(players: Array[Player], bracket: String) {
        var input = ""
        while (input != "q") {
            println(s"Enter one of the following options for your final analysis about the top ${players.length} players in the $bracket bracket")
            println("\nf: View the percentage of Horde players versus Alliance players")
            println("wl: View the average percentage of wins and losses, as well as the average games played")
            println("r: View a list and count of all realms (servers) that the top players are from")
            println("q: Return to the previous menu\n")
            input = readLine()
            println()
            try {
                input match {
                    case "f" => {
                        Player.hordeVsAlliance(players, bracket)
                        waitforUser()
                    }
                    case "r" => {
                        Player.countRealms(players, bracket)
                        waitforUser()
                    }
                    case "wl" => {
                        Player.winLossRatio(players, bracket)
                        waitforUser()
                    }
                    case "q" => println("Exiting detailed analysis...")
                    case input if checkValid(input)._1 == true => {
                        val results = checkValid(input)
                        quickAnalysis(results._2, results._3, results._4)
                    }
                    case _ => invalid
                }   
            } catch {
                case n: NumberFormatException => invalid
            }
        }
    }
}
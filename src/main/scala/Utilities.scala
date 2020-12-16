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

class Utilities()

object Utilities {
    /** Simple function used to check if a String can be converted into an Int
      * @param s the input String
      * @return Some(Int) if conversion is valid, or None if invalid
      */
    def toInt(s: String): Option[Int] = {
        try {
            Some(s.toInt)
        } catch {
            case e: Exception => None
        }
    }

    /**
      * Simple function that program to pause while user looks at results
      */
    def waitforUser() {
        println("\nPress enter to resume")
        readLine()
    }

    /** Function used to start the application
      */
    def start() {
        //Referenced https://users.scala-lang.org/t/executing-an-action-after-everything-in-a-list-of-futures-has-completed/1607/2
        //for the following Future implementation
        val twos = Future {fetchData("2v2"); 1}
        val threes = Future {fetchData("3v3"); 2}
        val rbgs = Future {fetchData("rbg"); 3}
        val result = for {
            r1 <- twos
            r2 <- threes
            r3 <- rbgs
        } yield (r1 + r2 + r3)
        println()
        println("Welcome to Cody Piazza's project zero!")
        Thread.sleep(2000)
        println()
        println("This project will analyze the current World of Warcraft PvP season.")
        Thread.sleep(2000)
        println()
        scala.concurrent.Await.ready(result, 10.seconds)
        result.onComplete {
            case Success(number) => 
            case Failure(exception) => {
                println("Failure! Data took too long to be retrieved!")
                exception.printStackTrace
            }
        }
        beginAnalysis()
    }
    def fetchData(bracket: String) {
        val tokenRequest = s"curl -s -u ${sys.env.get("ID").get}:${sys.env.get("SECRET").get} -d grant_type=client_credentials https://us.battle.net/oauth/token"
        val token = tokenRequest.!!.split("\"")(3)
        val url = s"https://us.api.blizzard.com/data/wow/pvp-season/30/pvp-leaderboard/$bracket?namespace=dynamic-us&locale=en_US&access_token=$token"
        val writer = new PrintWriter(s"$bracket.json")
        val apiInfo = scala.io.Source.fromURL(url).mkString.parseJson.prettyPrint
        writer.print(apiInfo)
        writer.close()
    }

    /** Checks if the user's input is a valid attempt at getting a quick analysis
      *     If an attempt was detected, the information returned by this function will be used in the quickAnalysis function
      * Only checks for partial validity, as partial validity implies that a user attempted to use the quick analysis feature
      * Later, in the quickAnalysis function, user's input will be checked for full validity.
      * @param s user's input string
      * @return tuple containing:
      *     Boolean -> Will be true if it is determined that a user attempted to run a quick analysis
      *     String -> Will contains the user's input bracket, or "" if no bracket was detected or bracket was input incorrectly
      *     Int -> Will contain the user's input player count, or 0 if no player count was detected or player count was input incorrectly
      *     String -> Will contain the user's input analysis type, or "" if no analysis type was detected or analysis type was input incorrectly
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

    def quickAnalysis(bracket: String, count: Int, analysisType: String) {
        if (bracket == "" || count == 0 || analysisType == "") {
            println("Input invalid! Are you attempting to do a quick analysis?\nQuick analysis format -> 2v2|3v3|rbg 1-4500 f|wl|r\nExample: 2v2 500 f")
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
    }
    def beginAnalysis() {
        breakable { while (true) {
            println("Options:")
            println("2v2 --> Analyze the current US 2v2 arena bracket")
            println("3v3 --> Analyze the current US 3v3 arena bracket")
            println("rbg --> Analyze the current US rated battleground season")
            println("q --> Exit application\n")
            val desiredCount = readLine()
            println()
            desiredCount match {
                case "2v2" => pvpAnalysis("2v2")
                case "3v3" => pvpAnalysis("3v3")
                case "rbg" => pvpAnalysis("rbg")
                case "q" => {println("\nExiting...\n"); break}
                case desiredCount if checkValid(desiredCount)._1 == true => {
                    val results = checkValid(desiredCount)
                    quickAnalysis(results._2, results._3, results._4)
                }
                case _ => {
                    println("Invalid input! Try again.")
                    waitforUser()
                }
            }
        } }
    }

    /** Function runs when user selects their bracket to analyze
      * @param bracket the pvp bracket to analyze, either 2v2, 3v3, or rbg (for rated battlegrounds)
      * While loop continues as long as user input is valid (valid Int between 1 and 4500) or until users types 'q' to quit
      * 
      * - Makes API call to Blizzard's PvP Leaderboards API and saves it to local cvs file
      * - Calls function "getPlayerInfo" to grab relevant player information from csv file
      *       Generates an array containing all relevant player data in the form of "Player" objects
      * - Calls function "analyze" to perform some basic analysis on the data within the array of "Player" objects
      */
    def pvpAnalysis(bracket: String) {
        var input = "" 
        while (input != "q") {
            println(s"q --> Exit $bracket analysis")
            println(s"Enter the number of top players you wish to analyze in the $bracket bracket (max 4500):\n")

            input = readLine()
            println()
            val players = toInt(input)

            if (players != None && players.get > 0 && players.get < 4501) { 
                val playerInfo = Player.getPlayerInfo(players.get, bracket)
                analyze(playerInfo, bracket)
            }
            else if (input == "q") println(s"\nExiting $bracket analysis...")
            else if (checkValid(input)._1 == true) {
                val results = checkValid(input)
                quickAnalysis(results._2, results._3, results._4)
            }
            else {
                println("Invalid input! Try again.")
                waitforUser()
            }
        }
    }

    /** Performs final analysis of user specified bracket and player count
      * @param players player array that will be used as a total count of players as well as arguments for functions
      * @param bracket PvP bracket previously specified by user to be used as argument for functions
      * user specifies which function to run, which will perform the basic analysis selected
      */
    def analyze(players: Array[Player], bracket: String) {
        var input = ""
        while (input != "q") {
            println(s"What do you wish to know about the top ${players.length} players in the $bracket bracket?")
            println("\nf --> Faction: Alliance to Horde ratio")
            println("wl--> Win/Loss: Average win/loss ratio")
            println("r --> Realms: Count of all realms that the top players are from")
            println("q --> Quit: Exit analysis\n")
            input = readLine()
            println()
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
                case _ => {
                    println("Invalid input! Try again.")
                    waitforUser()
                }
            }
        }
    }
}
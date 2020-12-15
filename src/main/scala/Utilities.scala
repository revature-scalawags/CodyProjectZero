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
        println()
        println("Press enter to resume")
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
        val tokenRequest = "curl -s -u " + sys.env.get("ID").get + ":" + sys.env.get("SECRET").get + " -d grant_type=client_credentials https://us.battle.net/oauth/token"
        val token = tokenRequest.!!.split("\"")(3)
        val url = "https://us.api.blizzard.com/data/wow/pvp-season/30/pvp-leaderboard/" + bracket + "?namespace=dynamic-us&locale=en_US&access_token=" + token
        val writer = new PrintWriter(bracket + ".csv")
        val apiInfo = scala.io.Source.fromURL(url).mkString.parseJson.prettyPrint
        writer.print(apiInfo)
        writer.close()
    }

    def validFull(s: String): (Boolean, String, Int, String) = {
        (false, "", 0, "")
    }
    def quickAnalysis(bracket: String, count: Int, analysisType: String) {

    }
    def beginAnalysis() {
        breakable { while (true) {
            println("Options:")
            println("2v2 --> Analyze the current US 2v2 arena bracket")
            println("3v3 --> Analyze the current US 3v3 arena bracket")
            println("rbg --> Analyze the current US rated battleground season")
            println("q --> Exit application")
            val desiredCount = readLine()
            desiredCount match {
                case "2v2" => pvpAnalysis("2v2")
                case "3v3" => pvpAnalysis("3v3")
                case "rbg" => pvpAnalysis("rbg")
                case "q" => {println("\nExiting...\n"); break}
                case desiredCount if validFull(desiredCount)._1 == true => {

                }
                case _ => println("\nInvalid input! Try again!")
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
            println()
            println("q --> Exit " + bracket + " analysis")
            println("Enter the number of top players you wish to analyze in the " + bracket + " bracket (max 4500): ")

            val file = io.Source.fromFile(bracket + ".csv")
            input = readLine()
            val players = toInt(input)

            if (players != None && players.get > 0 && players.get < 4501) { 
                val lines = file.getLines().drop(11).take(players.get * 28)
                val playerInfo = Player.getPlayerInfo(lines, players.get)
                analyze(playerInfo, bracket)
            }
            else if (input == "q") {
                println()
                println("Exiting " + bracket + " analysis...")
            }
            else {
                println()
                println("Invalid input! Try again.")
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
            println()
            println("What do you wish to know about the top " + players.length + " players in the " + bracket + " bracket?")
            println()
            println("f --> Faction: Alliance to Horde ratio")
            println("wl--> Win/Loss: Average win/loss ratio")
            println("r --> Realms: Count of all realms that the top players are from")
            println("q --> Quit: Exit analysis")
            input = readLine()
            println()
            if (input == "f") {
                Player.hordeVsAlliance(players, bracket)
                waitforUser()
            }
            else if (input == "r") {
                Player.countRealms(players, bracket)
                waitforUser()
            }
            else if (input == "wl") {
                Player.winLossRatio(players, bracket)
                waitforUser()
            }
            else if (input == "q") println("Exiting detailed analysis...")
            else {
                println()
                println("Invalid input! Try again.")
            }
        }
    }
}
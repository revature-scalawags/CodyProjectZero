import scalaj.http._
import scala.sys.process._
import scala.util.control.Breaks._
import scala.io.StdIn.readLine
import spray.json._
import DefaultJsonProtocol._
import java.io.PrintWriter
import scala.collection.mutable._
import java.io.FileNotFoundException

object Project0 extends App {
    start()

    /** Function used to start the application
      */
    def start() {
        println()
        println("Welcome to Cody Piazza's project zero!")
        //Thread.sleep(1000)
        println()
        println("This project will analyze the current World of Warcraft PvP season.")
        //Thread.sleep(1000)
        println()
         breakable { while (true) {
            println("Options:")
            println("2 --> Analyze the current US 2v2 arena bracket")
            println("3 --> Analyze the current US 3v3 arena bracket")
            println("10 --> Analyze the current US rated battleground season")
            println("q --> Exit application")
            val desiredCount = readLine()
            if (desiredCount == "2") pvpAnalysis("2v2") 
            else if (desiredCount == "3") pvpAnalysis("3v3")
            else if (desiredCount == "10") pvpAnalysis("rbg")
            else if (desiredCount == "q") {
                println("Exiting...")
                break
            }
            else println("Invalid input! Try again")
        } }
    }
    

    /** Simple function used to check if a String can be converted into an Int
      *
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
    
    /** Function runs if user selects 3v3 analysis
      * 
      * While loop continues as long as user input is valid (valid Int between 1 and 4500) or until users types 'q' to quit
      * 
      * - Parses CSV file for x amount of characters (or x * 29 lines) where x is the user's input number
      * - Generates an array containing all character IDs that were parsed
      * - Prints this array to user
      * 
      */
    def pvpAnalysis(bracket: String) {
        var input = "" // User input variable

        // Makes API call to get relevant pvp data
        println()
        println("Grabbing data...")
        println()
        val tokenRequest = "curl -u " + sys.env.get("ID").get + ":" + sys.env.get("SECRET").get + " -d grant_type=client_credentials https://us.battle.net/oauth/token"
        val token = tokenRequest.!!.split("\"")(3)
        val url = "https://us.api.blizzard.com/data/wow/pvp-season/30/pvp-leaderboard/" + bracket + "?namespace=dynamic-us&locale=en_US&access_token=" + token
        val writer = new PrintWriter(bracket + ".csv")
        val apiInfo = scala.io.Source.fromURL(url).mkString.parseJson.prettyPrint
        writer.print(apiInfo)
        writer.close()

        while (input != "q") {
            //Requests input from user
            println()
            println("q --> Exit " + bracket + " analysis")
            println("Enter the number of top players you wish to analyze (max 4500): ")


            val file = io.Source.fromFile(bracket + ".csv") // File that contains the relevant data
            input = readLine() // User's input
            val players = toInt(input) // Checks if input is a number

            if (players != None && players.get > 0 && players.get < 4501) { // Checks for valid input
                val lines = file.getLines().drop(11).take(players.get * 28) // The lines that will be read from the relevant csv file
                val playerInfo = getPlayerInfo(lines, players.get)
                analyze(playerInfo, players.get)
                //val fillInfo = getCharacterInfo(playerInfo, token)
                //val hordeAlliance = hordeVsAlliance(lines, rank.get)
                //println(analyze(hordeAlliance))
            }
            else if (input == "q") println("Exiting " + bracket + " analysis")
            else println("Invalid input! Try again.") // Triggers if user input is not valid
        }
    }

    // Class used to organize player data retrieved
    class Player(val name: String, val realm: String, val faction: String, val rank: Int, val rating: Int, val wins: Int, val losses: Int)

    /** Retrieves relevant information from the API call to the PvP leaderboards
      * 
      *
      * @param lines lines in the relevant file that has been parsed and saved
      * @param players number of players to analyze, input by the user
      * @return array of player objects containing all relevant data for later analysis
      */
    def getPlayerInfo(lines: Iterator[String], players: Int) : Array[Player] = {
        val playerArray = new Array[Player](players)

        var name = ""
        var realm = ""
        var faction = ""
        var rank = 0
        var rating = 0
        var wins = 0
        var losses = 0

        var trim = ""
        var index = 0
        for (line <- lines) { // Loops through all selected lines
            if (line.contains("\"name\"")) {
                trim = line.trim()
                name = trim.substring(9, trim.length-2).toLowerCase()
            }
            if (line.contains("\"slug\"")) {
                trim = line.trim()
                realm = trim.substring(9, trim.length-1)
            }
            if (line.contains("\"type\"")) {
                trim = line.trim()
                faction = trim.substring(9, trim.length-1)
            }
            if (line.contains("\"rank\"")) {
                trim = line.trim()
                rank = trim.substring(8, trim.length-1).toInt
            }
            if (line.contains("\"rating\"")) {
                trim = line.trim()
                rating = trim.substring(10, trim.length-1).toInt
            }
            if (line.contains("\"lost\"")) {
                trim = line.trim()
                losses = trim.substring(8, trim.length-1).toInt
            }
            if (line.contains("\"won\"")) {
                trim = line.trim()
                wins = trim.substring(7).toInt
                playerArray(index) = new Player(name, realm, faction, rank, rating, wins, losses)
                index += 1
                name = ""
                realm = ""
                faction = ""
                rank = 0
                rating = 0
                wins = 0
                losses = 0
            }
        }
        // for (player <- playerArray) println("Name: " + player.name.capitalize + " || Realm: " + player.realm.capitalize + 
        // " || Faction: " + player.faction.toLowerCase().capitalize + " || Rank: " + player.rank + " || Rating: " + player.rating + 
        // " || Wins: " + player.wins + " || Losses: " + player.losses)
        playerArray
    }
    /** Iterates through relevant file and counts horde and alliance players
      * @param lines lines in the relevant file that has been parsed and saved
      * @param players number of players to analyze, input by the user
      * @return returns a map containing counts for horde players and alliance players
      */
    def hordeVsAlliance(players: Array[Player]) {
        val map = Map("Horde" -> 0, "Alliance" -> 0)
        for (player <- players) { // Loops through all selected lines
            if (player.faction.equalsIgnoreCase("ALLIANCE")) map("Alliance") += 1
            else if (player.faction.equalsIgnoreCase("HORDE")) map("Horde") += 1
        }
        val aPercent = ((map("Alliance").toDouble / players.length) * 100).round
        val hPercent = ((map("Horde").toDouble / players.length) * 100).round
        println("In the top " + players.length + " players in the US, " + aPercent + "% are Alliance and " + hPercent + "% are Horde.")
    }
    def countRealms(players: Array[Player]) {
        val map = Map[String, Int]()
        for (p <- players) {
            if (!map.contains(p.realm)) map += (p.realm -> 1)
            else map(p.realm) += 1
        }
        val sorted = ListMap(map.toSeq.sortWith(_._2 > _._2):_*)
        for ((k, v) <- sorted) println(k.capitalize + ": " + v)
    }
    def waitforUser() {
        println("Press enter to resume")
        readLine()
    }
    def analyze(players: Array[Player], count: Int) {
        var input = ""
        while (input != "q") {
            //Requests input from user
            println()
            println("What do you wish to know about the top " + count + " players?")
            println()
            println("f --> Faction: Alliance to Horde ratio")
            println("wl--> Win/Loss: Average win/loss ratio")
            println("r --> Realms: Count of all realms that the top players are from")
            println("q --> Quit: Exit analysis")

            input = readLine() // User's input

            if (input == "f") {
                hordeVsAlliance(players)
                waitforUser()
            }
            else if (input == "r") {
                countRealms(players)
                waitforUser()
            }
        }

    }

    // BELOW CODE IS NOT CURRENTLY BEING USED
    
    //Class used for the function that follows
    class Character(val name: String, val realm: String, val playable_class: String, val race: String, val current_spec: String)

    /** This method retrieves additional data about individual characters that showed up in the original API call
      * However, it currently takes too long to execute this function when the number of characters grows beyond 10 or so
      * As such, it will not be used until it can be optimized
      * @param charInfo base character information (name and realm) used to retrieve other data
      * @param token token generated from original API call to authorize additional calls
      * @return returns an array of more detailed character objects
      */
    def getCharacterInfo(charInfo: Array[Player], token: String): Array[Character] = {
        val results = new Array[Character](charInfo.size)
        var index = 0
        for (player <- charInfo) {
                val url = "https://us.api.blizzard.com/profile/wow/character/" + player.realm + "/" + player.name + "/appearance?namespace=profile-us&locale=en_US&access_token=" + token
                try {
                    val info = scala.io.Source.fromURL(url).mkString
                    var sub = info.substring(info.indexOf("\"name\"") + 7)
                    for (i <- 1 to 2) {
                        sub = sub.substring(sub.indexOf("\"name\"") + 7)
                    }
                    val race = sub.substring(1, sub.indexOf(",") - 1)
                    sub = sub.substring(sub.indexOf("\"name\"") + 7)
                    val playable_class = sub.substring(1, sub.indexOf(",") - 1)
                    sub = sub.substring(sub.indexOf("\"name\"") + 7)
                    val current_spec = sub.substring(1, sub.indexOf(",") - 1)
                    results(index) = new Character(player.name, player.realm, playable_class, race, current_spec)
                } catch {
                    case e: FileNotFoundException => {
                        println("Could not retrieve info for character \"" + player.name + "\"")
                        results(index) = new Character(player.name, player.realm, "unknown", "unknown", "unknown")
                    }
                }
                index += 1
        }
        for (i <- results) println(i.name)
        results
    }
}


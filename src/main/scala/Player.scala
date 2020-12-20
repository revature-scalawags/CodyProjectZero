package wowPvPAnalysis

import scala.collection.mutable._
import scala.io.BufferedSource
import com.typesafe.scalalogging.LazyLogging
import java.util.Date
import spray.json._
import DefaultJsonProtocol._
import org.mongodb.scala.bson.collection._

// Class used to organize player data retrieved from API call
class Player {
    private var _name: String = _
    private var _realm: String = _
    private var _faction: String = _
    private var _rank: Int = _
    private var _rating: Int = _
    private var _wins: Int = _
    private var _losses: Int = _

    def name = _name
    def name_=(name: String) {
        _name = name
    }
    def realm = _realm
    def realm_=(realm: String) {
        _realm = realm
    }
    def faction = _faction
    def faction_=(faction: String) {
        _faction = faction
    }
    def rank = _rank
    def rank_=(rank: Int) {
        _rank = rank
    }
    def rating = _rating
    def rating_=(rating: Int) {
        _rating = rating
    }
    def wins = _wins
    def wins_=(wins: Int) {
        _wins = wins
    }
    def losses = _losses
    def losses_=(losses: Int) {
        _losses = losses
    }
}
// Companion object
object Player extends LazyLogging {
    /** Retrieves relevant information from the API call to the PvP leaderboards.
      * @param lines Lines in the relevant file that has been parsed and saved.
      * @param players Number of players to analyze, input by the user.
      * @return Returns an array of player objects containing all relevant data for later analysis.
      */
    def getPlayerInfo(players: Int, bracket: String) : Array[Player] = {
        // Gets the relevant lines (first 11 are metadata, then grabs the lines following equal to the player count (players) * 28 (number of lines of data per character)).
        val lines = io.Source.fromFile(s"$bracket.json").getLines().drop(11).take(players * 28)

        val playerArray = new Array[Player](players) // The array of player objects that will be returned.
        var index = 0 // Will be used as a pointer to the correct location in the playerArray.
        var player = new Player() // First new player object.

        // Loops through all selected lines and grabs relevant data from them if such data exists.
        for (line <- lines) { 
            val trimmed = line.trim() // Trim the whitespace to make it easier to grab the lines desired.
            line match {
                case line if line.contains("\"name\"") => player.name_=(trimmed.substring(9, trimmed.lastIndexOf("\"")).toLowerCase()) 
                case line if line.contains("\"slug\"") => player.realm_=(trimmed.substring(9, trimmed.lastIndexOf("\"")))
                case line if line.contains("\"type\"") => player.faction_=(trimmed.substring(9, trimmed.lastIndexOf("\"")))
                case line if line.contains("\"rank\"") => player.rank_=(trimmed.substring(8, trimmed.lastIndexOf(",")).toInt)
                case line if line.contains("\"rating\"") => player.rating_=(trimmed.substring(10, trimmed.lastIndexOf(",")).toInt)
                case line if line.contains("\"lost\"") => player.losses_=(trimmed.substring(8, trimmed.lastIndexOf(",")).toInt)
                case line if line.contains("\"won\"") => {
                    player.wins_=(trimmed.substring(7).toInt)
                    playerArray(index) = player // Player object is now full, and can be added to the array.
                    player = new Player() // New player object for next iteration.
                    index += 1 // Add 1 to index so the next iteration points to the next spot in the playerArray.
                }
                case _ => () // Base case, do nothing.
            }
        }
        playerArray // Return fully populated player array.
    }

    /** Iterates through the entire input Player array, counting occurances of "HORDE" and "ALLIANCE",
      * prints percentages for Horde players and Alliance players to the console, saves the information to the database, and logs the transaction in the log.
      * @param players Array of Player objects, containing relevant player data parsed from Blizzard PvP API.
      * @param bracket The user specified bracket (2v2, 3v3, rbg).
      * @return Returns the document that will be added to the database. For testing purposes.
      */
    def hordeVsAlliance(players: Array[Player], bracket: String): Document =  {
        val map = Map("Horde" -> 0, "Alliance" -> 0) // Starting map that will contain counts of Horde and Alliance.
        for (player <- players) {
            if (player.faction.equalsIgnoreCase("ALLIANCE")) map("Alliance") += 1 // Add 1 to Alliance count.
            else if (player.faction.equalsIgnoreCase("HORDE")) map("Horde") += 1 // Add 1 to Horde count.
        }
        val aPercent = ((map("Alliance").toFloat / players.length) * 100).round
        val hPercent = ((map("Horde").toFloat / players.length) * 100).round
        println(s"In the top ${players.length} players in the $bracket bracket in the US:\n")
        println(s"$aPercent% are Alliance")
        println(s"$hPercent% are Horde")
        val doc = DB.fDoc(bracket, players.length, aPercent, hPercent) // Call DB.fDoc method to create the relevant document.
        DB.add(doc, "faction-ratio") // Add document to database.
        logger.info(s"Bracket: $bracket | Players: ${players.length} | Alliance %: $aPercent | Horde %: $hPercent") // Log the information in the log.
        doc
    }
    /** Iterates through the entire input Player array, counting occurances of each realm,
      * prints the realms sorted in ascending order to the console, saves a count of the total realms represented in the databse, and logs the transaction in the log.
      * @param players Array of Player objects, containing relevant player data parsed from Blizzard PvP API.
      * @param bracket The user specified bracket (2v2, 3v3, rbg).
      * @return Returns the document that will be added to the database.
      */
    def countRealms(players: Array[Player], bracket: String): Document = {
        val map = Map[String, Int]() // Map[RealmName, RealmCount]
        for (p <- players) {
            if (!map.contains(p.realm)) map += (p.realm -> 1)
            else map(p.realm) += 1
        }
        val sorted = Array(map.toArray.sortWith(_._2 < _._2):_*) //https://alvinalexander.com/scala/how-to-sort-map-in-scala-key-value-sortby-sortwith/
        println(s"The realms in the top ${players.length} players in the $bracket bracket in the US are:\n")
        for ((k, v) <- sorted) {
            println(k.capitalize + ": " + v) // Print results to console
        }
        val doc = DB.rDoc(bracket, players.length, map.size) // Call DB.rDoc method to create the relevant document
        DB.add(doc, "realm-counts") // Add document to database
        logger.info(s"Bracket: $bracket | Players: ${players.length} | Realms Represented: ${map.size}")// Log the information in the log
        doc
    }
    /** Iterates through the entire input Player array, counting wins and losses,
      * prints the average win and loss percentage, as well as the average games played, to the console, 
      * saves this information in the databse, and logs the transaction in the log.
      * @param players Array of Player objects, containing relevant player data parsed from Blizzard PvP API.
      * @param bracket The user specified bracket (2v2, 3v3, rbg).
      * @return Returns the document that will be added to the database. For testing purposes.
      */
    def winLossRatio(players: Array[Player], bracket: String): Document =  {
        val map = Map("Wins" -> 0, "Losses" -> 0)
        for (p <- players) {
            map("Wins") += p.wins
            map("Losses") += p.losses
        }
        val totalGames = map("Wins") + map("Losses")
        val wPercent = ((map("Wins").toFloat / totalGames) * 100).round
        val lPercent = ((map("Losses").toFloat / totalGames) * 100).round
        val averageGamesPlayed = totalGames / players.length
        println(s"For the top ${players.length} players in $bracket bracket in the US:\n")
        println(s"$wPercent% are victories")
        println(s"$lPercent% are losses")
        println(s"Average games played: $averageGamesPlayed")
        val doc = DB.wlDoc(bracket, players.length, wPercent, lPercent, averageGamesPlayed) // Call DB.wlDoc method to create the relevant document
        DB.add(doc, "win-loss-ratio") // Add document to database
        logger.info(s"Bracket: $bracket | Players: ${players.length} | Win %: $wPercent | Loss %: $lPercent | Average games: $averageGamesPlayed") // Log the infoamtion in the log
        doc
    }
}

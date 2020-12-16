package wowPvPAnalysis

import scala.collection.mutable._
import scala.io.BufferedSource
import com.typesafe.scalalogging.LazyLogging
import java.util.Date
import spray.json._
import DefaultJsonProtocol._
import org.mongodb.scala.bson.collection._

// Class used to organize player data retrieved
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
    object Player extends LazyLogging {
        /** Retrieves relevant information from the API call to the PvP leaderboards
         * @param lines lines in the relevant file that has been parsed and saved
         * @param players number of players to analyze, input by the user
         * @return array of player objects containing all relevant data for later analysis
         */
        def getPlayerInfo(players: Int, bracket: String) : Array[Player] = {
            val lines = io.Source.fromFile(s"$bracket.json").getLines().drop(11).take(players * 28)
            val playerArray = new Array[Player](players)

            var trim = ""
            var index = 0
            var player = new Player()
            for (line <- lines) { // Loops through all selected lines
                val trimmed = line.trim()
                line match {
                    case line if line.contains("\"name\"") => player.name_=(trimmed.substring(9, trimmed.lastIndexOf("\"")).toLowerCase())
                    case line if line.contains("\"slug\"") => player.realm_=(trimmed.substring(9, trimmed.lastIndexOf("\"")))
                    case line if line.contains("\"type\"") => player.faction_=(trimmed.substring(9, trimmed.lastIndexOf("\"")))
                    case line if line.contains("\"rank\"") => player.rank_=(trimmed.substring(8, trimmed.lastIndexOf(",")).toInt)
                    case line if line.contains("\"rating\"") => player.rating_=(trimmed.substring(10, trimmed.lastIndexOf(",")).toInt)
                    case line if line.contains("\"lost\"") => player.losses_=(trimmed.substring(8, trimmed.lastIndexOf(",")).toInt)
                    case line if line.contains("\"won\"") => {
                        player.wins_=(trimmed.substring(7).toInt)
                        playerArray(index) = player
                        player = new Player()
                        index += 1
                    }
                    case _ => ()
                }
            }
            playerArray
        }

        /** Iterates through the entire input Player array, counting occurances of "HORDE" and "ALLIANCE"
         * @param players array of Player objects, containing relevant player data parsed from Blizzard PvP API
         * prints % for horde players and alliance players
         */
        def hordeVsAlliance(players: Array[Player], bracket: String) {
            val map = Map("Horde" -> 0, "Alliance" -> 0)
            for (player <- players) { // Loops through all selected lines
                if (player.faction.equalsIgnoreCase("ALLIANCE")) map("Alliance") += 1
                else if (player.faction.equalsIgnoreCase("HORDE")) map("Horde") += 1
            }
            val aPercent = ((map("Alliance").toDouble / players.length) * 100).round
            val hPercent = ((map("Horde").toDouble / players.length) * 100).round
            println(s"In the top ${players.length} players in the $bracket bracket in the US:\n")
            println(s"$aPercent% are Alliance")
            println(s"$hPercent% are Horde")
            val doc = Document("Date and Time" -> new Date().toString(), "Bracket" -> bracket, "Player Count" -> players.length, "Alliance %" -> aPercent, "Horde %" -> hPercent)
            DB.add(doc, "faction-ratio")
            logger.info(s"Bracket: $bracket | Players: ${players.length} | Alliance %: $aPercent | Horde %: $hPercent")
        }
        /** Iterates through the entire input Player array, counting occurances of each realm
         * @param players array of Player objects, containing relevant player data parsed from Blizzard PvP API
         * prints counts of each realm occurance
         */
        def countRealms(players: Array[Player], bracket: String) {
            val map = Map[String, Int]()
            for (p <- players) {
                if (!map.contains(p.realm)) map += (p.realm -> 1)
                else map(p.realm) += 1
            }
            val sorted = Array(map.toArray.sortWith(_._2 < _._2):_*) //https://alvinalexander.com/scala/how-to-sort-map-in-scala-key-value-sortby-sortwith/
            println(s"The realms in the top ${players.length} players in the $bracket bracket in the US are:\n")
            for ((k, v) <- sorted) {

                println(k.capitalize + ": " + v)
            }
            val doc = Document("Date and Time" -> new Date().toString(), "Bracket" -> bracket, "Player Count" -> players.length, "Realms Represented" -> map.size)
            DB.add(doc, "realm-counts")
            logger.info(s"Bracket: $bracket | Players: ${players.length} | Realms Represented: ${map.size}")
        }
        /** Iterates through the entire input Player array, counting wins and losses
         * @param players array of Player objects, containing relevant player data parsed from Blizzard PvP API
         * prints average win %, loss %, and average games played
         */
        def winLossRatio(players: Array[Player], bracket: String) {
            val map = Map("Wins" -> 0, "Losses" -> 0)
            for (p <- players) {
                map("Wins") += p.wins
                map("Losses") += p.losses
            }
            val totalGames = map("Wins") + map("Losses")
            val wPercent = ((map("Wins").toDouble / totalGames) * 100).round
            val lPercent = ((map("Losses").toDouble / totalGames) * 100).round
            val averageGamesPlayed = totalGames / players.length
            println(s"For the top ${players.length} players in $bracket bracket in the US:\n")
            println(s"$wPercent% are victories")
            println(s"$lPercent% are losses")
            println(s"Average games played: $averageGamesPlayed")
            val doc = Document("Date and Time" -> new Date().toString(), "Bracket" -> bracket, "Player Count" -> players.length,
             "Win %" -> wPercent, "Loss %" -> lPercent, "Average Games Played" -> averageGamesPlayed)
            DB.add(doc, "win-loss-ratio")
            logger.info(s"Bracket: $bracket | Players: ${players.length} | Win %: $wPercent | Loss %: $lPercent | Average games: $averageGamesPlayed")
        }
    }

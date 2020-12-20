package wowPvPAnalysis

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec

//Below tests only works if you have test json files (2v2test, 3v3test, rbgtest) with data in it
class PlayerTest extends AnyFlatSpec {
    //Testing Player.getPlayerInfo method
    "Player" should "retrieve player information" in {
        val players = Player.getPlayerInfo(10, "2v2test")
        assert(players.length == 10)
        assert(players(0).name.equals("smxn"))
        assert(players(9).name.equals("mooncucumber"))
        assert(players(0).realm.equals("thrall"))
        assert(players(9).realm.equals("malganis"))
        assert(players(0).faction.equals("HORDE"))
        assert(players(9).faction.equals("HORDE"))
        assert(players(0).rank.equals(1))
        assert(players(9).rank.equals(10))
        assert(players(0).rating.equals(2497))
        assert(players(9).rating.equals(2400))
        assert(players(0).wins.equals(94))
        assert(players(9).wins.equals(137))
        assert(players(0).losses.equals(29))
        assert(players(9).losses.equals(91))
    }
    //Testing Player.hordeVsAlliance method
    it should "compute win/loss ratio for specific input bracket and player count" in {
        val players = Player.getPlayerInfo(10, "3v3test")
        val doc = Player.hordeVsAlliance(players, "3v3test").toJson()
        assert(doc.contains("Date and Time"))
        assert(doc.contains("\"Bracket\": \"3v3test\", \"Player Count\": 10, \"Alliance %\": 40, \"Horde %\": 60"))
    }
    //Testing Player.winLossRatio method
    it should "compute Horde/Alliance ratio for specific input bracket and player count" in {
        val players = Player.getPlayerInfo(10, "3v3test")
        val doc = Player.winLossRatio(players, "3v3test").toJson()
        assert(doc.contains("Date and Time"))
        assert(doc.contains("\"Bracket\": \"3v3test\", \"Player Count\": 10, \"Win %\": 69, \"Loss %\": 31, \"Average Games Played\": 250"))
    }
    //Testing Player.countRealms method
    it should "tally total represented realms" in {
        val players = Player.getPlayerInfo(10, "rbgtest")
        val doc = Player.countRealms(players, "rbgtest").toJson()
        assert(doc.contains("Date and Time"))
        assert(doc.contains("\"Bracket\": \"rbgtest\", \"Player Count\": 10, \"Realms Represented\": 7"))
    }
}
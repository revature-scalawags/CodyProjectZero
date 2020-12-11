import scalaj.http._
import scala.sys.process._
import scala.io.StdIn.readLine

object Project0 extends App {
    var alliance = 0
    var horde = 0

    // val cmd = Seq("curl", "-u", ":","-d", "grant_type=client_credentials","https://us.battle.net/oauth/token")
    // val output = cmd.!!
    // println("Ermagurd heres the output! : " + output)

    println("Welcome to Cody Piazza's project zero!")
    //Thread.sleep(1000)
    println()
    println("This project will analyze the current World of Warcraft pvp season.")
    //Thread.sleep(1000)
    println()
    while (true) {
        println("Options:")
        println("2 --> Analyze the current 2v2 arena bracket")
        println("3 --> Analyze the current 3v3 arena bracket")
        val desiredCount = readLine()
        if (desiredCount == "2" || desiredCount == "3") println("Valid!")
        else println("Invalid!")
    }
    

    val threes = io.Source.fromFile("threes.csv")
    for (line <- threes.getLines()) {
        if (line.contains("ALLIANCE")) alliance += 1
        if (line.contains("HORDE")) horde += 1
    }
    println("Total alliance in top 5000: " + alliance)
    println("Total horde in top 5000: " + horde)
}
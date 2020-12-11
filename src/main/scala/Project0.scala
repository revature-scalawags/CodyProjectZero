import scalaj.http._
import scala.sys.process._
import scala.io.StdIn.readLine

object Project0 extends App {
    // val cmd = Seq("curl", "-u", ":","-d", "grant_type=client_credentials","https://us.battle.net/oauth/token")
    // val output = cmd.!!
    // println("Ermagurd heres the output! : " + output)

    start()

    /**
      * Function used to start the application
      */
    def start() {
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
            if (desiredCount == "2") println("Valid!")
            else if (desiredCount == "3") threesAnalysis()
            else println("Invalid!")
        }
    }
    

    /**
      * Simple function used to check if a String can be converted into an Int
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
    
    /**
      * Function runs if user selects 3v3 analysis
      * 
      * While loop continues as long as user input is valid (valid Int between 1 and 4500) or until users types 'q' to quit
      * 
      * - Parses CSV file for x amount of characters (or x * 29 lines) where x is the user's input number
      * - Generates an array containing all character IDs that were parsed
      * - Prints this array to user
      * 
      */
    def threesAnalysis() {
        var input = "" //User input variable
        while (input != "q") {
            println("Type 'q' to exit 3v3 analysis")
            println("Enter the number of ranks you wish to analyze (max 4500): ")
            println("Example - Typing '1000' would analyze the top 1000 players")
            val threes = io.Source.fromFile("threes.csv")
            input = readLine()
            val rank = toInt(input)
            if (rank != None && rank.get > 0 && rank.get < 4501) {
                val lines = threes.getLines().drop(18).take(rank.get * 29)
                var nextIsCharID = false
                var index = 0
                val charIDs = new Array[String](rank.get)
                for (line <- lines) {
                    if (nextIsCharID) {
                        val trimmedLine = line.trim()
                        charIDs(index) = trimmedLine.substring(6, trimmedLine.length - 1)
                        nextIsCharID = false
                        index += 1
                    }
                    if (line.contains("\"name\":")) nextIsCharID = true
                    
                    
                }
                for (item <- charIDs) print(item + " ")
                println(charIDs.length + " character IDs scraped")
            }
            else println("Invalid input! Try again.")
        }
    }
    
}
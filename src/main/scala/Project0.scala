import scalaj.http._
import scala.sys.process._

object Project0 extends App {
    var alliance = 0
    var horde = 0

    // val cmd = Seq("curl", "-u", "0a3af65843374ce78fcbd94f4791164c:1V5TKrj3QUZsRIDE3b3PaI3UMyh27eHJ","-d", "grant_type=client_credentials","https://us.battle.net/oauth/token")
    // val output = cmd.!!
    // println("Ermagurd heres the output! : " + output)


    val threes = io.Source.fromFile("threes.csv")
    for (line <- threes.getLines()) {
        if (line.contains("ALLIANCE")) alliance += 1
        if (line.contains("HORDE")) horde += 1
    }
    println("Total alliance in top 5000: " + alliance)
    println("Total horde in top 5000: " + horde)
}
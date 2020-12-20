package wowPvPAnalysis

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import java.io.ByteArrayOutputStream
import java.awt.Robot
import java.awt.event.KeyEvent

class UtilityTest extends AnyFlatSpec {
    //Testing Utilities.fetchData method
    "Utilities" should "fetch PvP data from the WoW API" in {
        val check2v2_1 = Future {assert(Utilities.fetchData("2v2").toList(1).contains("_links"))}
        val check2v2_2 = Future {assert(Utilities.fetchData("2v2").toList(34).contains("key"))}
        val check3v3_1 = Future {assert(Utilities.fetchData("3v3").toList(1).contains("_links"))}
        val check3v3_2 = Future {assert(Utilities.fetchData("3v3").toList(34).contains("key"))}
        val checkrbg_1 = Future {assert(Utilities.fetchData("rbg").toList(1).contains("_links"))}
        val checkrbg_2 = Future {assert(Utilities.fetchData("rbg").toList(34).contains("key"))}
        val result = for {
            r1 <- check2v2_1 
            r2 <- check2v2_2
            r3 <- check3v3_1
            r4 <- check3v3_2 
            r5 <- checkrbg_1
            r6 <- checkrbg_2
        } yield (r1, r2, r3, r4, r5, r6)

        scala.concurrent.Await.ready(result, 10.seconds)  
        result.onComplete {
            case Success(number) => println("All fetches check out!")
            case Failure(exception) => {
                println("Future Failed. Boooo.")
                exception.printStackTrace
            }
        }
    }
    //Testing Utilities.checkValid method
    it should "check for partial validity when parsing for a quick analysis" in {
        // 100% valid
        assert(Utilities.checkValid("2v2 1 f")._1 == true)
        assert(Utilities.checkValid("3v3 4500 r")._1 == true)
        assert(Utilities.checkValid("rbg 500 wl")._1 == true)

        // 66% good enough
        assert(Utilities.checkValid("2v2 1 d")._1 == true)
        assert(Utilities.checkValid("3v3 4501 f")._1 == true)
        assert(Utilities.checkValid("1v1 4500 wl")._1 == true)

        // Only 33% valid, not good enough
        assert(Utilities.checkValid("rbg 0 d")._1 == false)
        assert(Utilities.checkValid("1v1 0 r")._1 == false)
        assert(Utilities.checkValid("1v1 500 d")._1 == false)

        // 0% valid
        assert(Utilities.checkValid("4v4 0 d")._1 == false)
    }
    //Testing Utilities.quickanalysis method
    it should "perform a quick analysis if proper syntax is input, " +
      "or output a helpful message if partially valid (66% valid) syntax is given" in {
        val robot = new Robot
        val valid = "Valid syntax"
        val invalid = "Input invalid! Are you attempting to do a quick analysis?\nQuick analysis format -> 2v2|3v3|rbg 1-4500 f|wl|r\nExample: 2v2 500 f"
        robot.keyPress(KeyEvent.VK_ENTER)
        robot.keyRelease(KeyEvent.VK_ENTER)
        assert(Utilities.quickAnalysis("2v2", 1, "f").equals(valid))
        robot.keyPress(KeyEvent.VK_ENTER)
        robot.keyRelease(KeyEvent.VK_ENTER)
        assert(Utilities.quickAnalysis("3v3", 4500, "").equals(invalid))
        robot.keyPress(KeyEvent.VK_ENTER)
        robot.keyRelease(KeyEvent.VK_ENTER)
        assert(Utilities.quickAnalysis("rbg", 0, "wl").equals(invalid))
        robot.keyPress(KeyEvent.VK_ENTER)
        robot.keyRelease(KeyEvent.VK_ENTER)
        assert(Utilities.quickAnalysis("", 500, "r").equals(invalid))
    }
}
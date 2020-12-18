package wowPvPAnalysis

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.mongodb.scala.bson.collection.immutable.Document
import com.mongodb.client.result.InsertOneResult

class DBTest extends AnyFlatSpec {
    // Testing DB.add method
    "DB" should "add a document to the database" in {
        val doc = Document("Test" -> "Result")
        assert(DB.add(doc, "test-collection").toString.contains("AcknowledgedInsertOneResult"))
        
    }
    //Testing DB.removeRecord method
    it should "delete a document from the database" in {
        assert(DB.removeRecord("test-collection", "Test", "Result").toString.contains("AcknowledgedDeleteResult{deletedCount=1}"))
    }
    //Testing DB.getAllByAnalysis method
    it should "retrieve all results of an analysis type from the database" in {
        val doc = Document("Test" -> "Result12345")
        DB.add(doc, "test-collection")
        assert(DB.getAllByAnalysis("test-collection").toString.contains("Result12345"))
        DB.removeRecord("test-collection", "Test", "Result12345")
    }
    //Testing DB.fDoc method
    it should "create a document denoting faction ratio information" in {
        val doc = DB.fDoc("2v2", 100, 30, 70).toJson()
        assert(doc.contains("Date and Time"))
        assert(doc.contains("\"Bracket\": \"2v2\", \"Player Count\": 100, \"Alliance %\": 30, \"Horde %\": 70"))
    }
    //Testing DB.wlDoc method
    it should "create a document denoting win/loss information" in {
        val doc = DB.wlDoc("3v3", 2000, 65, 35, 125).toJson()
        assert(doc.contains("Date and Time"))
        assert(doc.contains("\"Bracket\": \"3v3\", \"Player Count\": 2000, \"Win %\": 65, \"Loss %\": 35, \"Average Games Played\": 125"))
    }
    //Testing DB.rDoc method
    it should "create a document denoting represented realms" in {
        val doc = DB.rDoc("rbg", 4500, 100).toJson()
        assert(doc.contains("Date and Time"))
        assert(doc.contains("\"Bracket\": \"rbg\", \"Player Count\": 4500, \"Realms Represented\": 100"))
    }
}
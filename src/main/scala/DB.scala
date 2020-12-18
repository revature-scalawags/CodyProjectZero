package wowPvPAnalysis

import com.typesafe.scalalogging.LazyLogging
import org.mongodb.scala._
import java.util.Date
import org.mongodb.scala._
import org.mongodb.scala.model._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.UpdateOptions
import org.mongodb.scala.bson.BsonObjectId
import org.mongodb.scala.bson.BsonDocument
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.DeleteResult
import tour.Helpers._

class DB extends LazyLogging 

object DB extends LazyLogging {
    def wlDoc(bracket: String, playerCount: Int, wPercent: Int, lPercent: Int, averageGames: Int): Document = {
        val doc = Document("Date and Time" -> new Date().toString(), "Bracket" -> bracket, "Player Count" -> playerCount,
             "Win %" -> wPercent, "Loss %" -> lPercent, "Average Games Played" -> averageGames)
        doc
    }
    def fDoc(bracket: String, playerCount: Int, aPercent: Int, hPercent: Int): Document = {
        val doc = Document("Date and Time" -> new Date().toString(), "Bracket" -> bracket, "Player Count" -> playerCount, 
        "Alliance %" -> aPercent, "Horde %" -> hPercent)
        doc
    }
    def rDoc(bracket: String, playerCount: Int, realms: Int): Document = {
        val doc = Document("Date and Time" -> new Date().toString(), "Bracket" -> bracket, "Player Count" -> playerCount, "Realms Represented" -> realms)
        doc
    }
    def add(doc: Document, collectionName: String): Seq[InsertOneResult] = {
        val uri: String = s"mongodb+srv://${sys.env.get("DB_USER").get}:${sys.env.get("DB_PASS").get}@cluster0.flmb4.mongodb.net/${sys.env.get("DB_NAME").get}?retryWrites=true&w=majority"
        val client: MongoClient = MongoClient(uri)
        val db: MongoDatabase = client.getDatabase("wow-pvp-analysis")
        val col = db.getCollection(collectionName)
        
        val results = col.insertOne(doc).results()
        client.close()
        results
    }
    def getAllByAnalysis(collectionName: String): Seq[Document] = {
        val uri: String = s"mongodb+srv://${sys.env.get("DB_USER").get}:${sys.env.get("DB_PASS").get}@cluster0.flmb4.mongodb.net/${sys.env.get("DB_NAME").get}?retryWrites=true&w=majority"
        val client: MongoClient = MongoClient(uri)
        val db: MongoDatabase = client.getDatabase("wow-pvp-analysis")
        val col = db.getCollection(collectionName)
        
        col.find().printResults()
        val results = col.find().results()
        client.close()
        results
    }

    def removeRecord(collectionName: String, field: String, value: String): Seq[DeleteResult] =  {
        val uri: String = s"mongodb+srv://${sys.env.get("DB_USER").get}:${sys.env.get("DB_PASS").get}@cluster0.flmb4.mongodb.net/${sys.env.get("DB_NAME").get}?retryWrites=true&w=majority"
        val client: MongoClient = MongoClient(uri)
        val db: MongoDatabase = client.getDatabase("wow-pvp-analysis")
        val col = db.getCollection(collectionName)
        val results = col.deleteOne(equal(field, value)).results()
        client.close()
        results
    }
}
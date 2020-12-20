package wowPvPAnalysis

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

class DB () 

object DB {
    /** Creates a document that will contain player win/loss information.
      * @param bracket The user specified bracket (2v2, 3v3, or rbg).
      * @param playerCount The user specified player count (1-4500).
      * @param wPercent The average percentage of games won, previously calculated based on user input.
      * @param lPercent The average percentage of games lost, previously calculated based on user input.
      * @param averageGames The average number of games played, previously calculated based on user input.
      * @return Returns the completed document, ready to be inserted into the database inside the win-loss-ratio collection.
      */
    def wlDoc(bracket: String, playerCount: Int, wPercent: Int, lPercent: Int, averageGames: Int): Document = {
        val doc = Document("Date and Time" -> new Date().toString(), "Bracket" -> bracket, "Player Count" -> playerCount,
             "Win %" -> wPercent, "Loss %" -> lPercent, "Average Games Played" -> averageGames)
        doc
    }

    /** Creates a document that will contain player faction ratio information.
      * @param bracket The user specified bracket (2v2, 3v3, or rbg).
      * @param playerCount The user specified player count (1-4500).
      * @param aPercent The average percentage of Alliance players, previously calculated based on user input.
      * @param hPercent The average percentage of Horde players, previously calculated based on user input.
      * @return Returns the completed document, ready to be inserted into the database inside the faction-ratio collection.
      */
    def fDoc(bracket: String, playerCount: Int, aPercent: Int, hPercent: Int): Document = {
        val doc = Document("Date and Time" -> new Date().toString(), "Bracket" -> bracket, "Player Count" -> playerCount, 
        "Alliance %" -> aPercent, "Horde %" -> hPercent)
        doc
    }

    /** Creates a document that will contain player realm information.
      * @param bracket The user specified bracket (2v2, 3v3, or rbg).
      * @param playerCount The user specified player count (1-4500).
      * @param aPercent The number of realms represented based on the user's previously specified input.
      * @return Returns the completed document, ready to be inserted into the database inside the realm-count collection.
      */
    def rDoc(bracket: String, playerCount: Int, realms: Int): Document = {
        val doc = Document("Date and Time" -> new Date().toString(), "Bracket" -> bracket, "Player Count" -> playerCount, "Realms Represented" -> realms)
        doc
    }

    /** Opens a connection to the DB client, adds the specified document to the specified collection, 
      * closes the connection, and returns the results of the query.
      * @param doc The document to add to the collection in the database.
      * @param collectionName The name of the collection in the database.
      * @return Returns a sequence of type InsertOneResult (the results of a MongoDB insertOne operation). For testing purposes.
      */
    def add(doc: Document, collectionName: String): Seq[InsertOneResult] = {
        val uri: String = s"mongodb+srv://${sys.env.get("DB_USER").get}:${sys.env.get("DB_PASS").get}@cluster0.flmb4.mongodb.net/${sys.env.get("DB_NAME").get}?retryWrites=true&w=majority"
        val client: MongoClient = MongoClient(uri)
        val db: MongoDatabase = client.getDatabase("wow-pvp-analysis")
        val col = db.getCollection(collectionName)
        
        val results = col.insertOne(doc).results()
        client.close()
        results
    }

    /** Opens a connection to the DB client, grabs all documents in a specified collection, prints the results to the console, 
      * closes the connection, and returns the results of the query.
      * @param collectionName The name of the collection in the database.
      * @return Returns the results as a sequence of documents. For testing purposes.
      */
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

    /** Opens a connection to the DB client, deletes a specified record from the specified collection, 
      * closes the connection, and returns the results of the query.
      * @param collectionName The name of the collection in the database.
      * @param field The field name of the item that will be deleted.
      * @param value The matching value of the item that will be deleted.
      * @return Returns the results as a sequence of type DeleteResult (the results of a MongoDB delete operation). For testing purposes.
      */
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
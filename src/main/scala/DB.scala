package wowPvPAnalysis

import com.typesafe.scalalogging.LazyLogging
import org.mongodb.scala._
import wowPvPAnalysis.Helpers._
import java.util.concurrent.TimeoutException

class DB extends LazyLogging 

object DB extends LazyLogging {
    def add(doc: Document, collectionName: String) {
        val uri: String = s"mongodb+srv://${sys.env.get("DB_USER").get}:${sys.env.get("DB_PASS").get}@cluster0.flmb4.mongodb.net/${sys.env.get("DB_NAME").get}?retryWrites=true&w=majority"
        val client: MongoClient = MongoClient(uri)
        val db: MongoDatabase = client.getDatabase("wow-pvp-analysis")
        val col = db.getCollection(collectionName)
        col.insertOne(doc).results()
    }
}
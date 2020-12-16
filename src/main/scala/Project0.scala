package wowPvPAnalysis

import scala.collection.mutable._
import java.io.FileNotFoundException
import org.mongodb.scala._
import org.mongodb.scala.Helpers
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Sorts._

object Project0 extends App {
    Utilities.start()
    // val mongoClient = MongoClient("mongodb://mongo")
    // val database = mongoClient.getDatabase("Project0")
    // val results = database.getCollection("Results")

    // val document = Document(
    //     "Name" -> "Kellsien",
    //     "Server" -> "Wyrmrest Accord",
    //     "Class" -> "Monk",
    //     "Spec" -> "Windwalker"
    // )

    // val record = results.find().projection(fields(include("Name"), excludeId())).limit(1)
    
    // println(record)
}


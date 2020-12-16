lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.13.3"
    )),
    name := "scalatest-example"
  )

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % Test
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.6"
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0"

name := "Lab1"

version := "0.1"

scalaVersion := "2.13.4"

val AkkaVersion = "2.5.31"
val AkkaHttpVersion = "10.1.11"
libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-sse" % "2.0.2",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",
  "com.lihaoyi" %% "upickle" % "0.9.5",
  "io.netty" % "netty-all" % "4.0.4.Final"
)

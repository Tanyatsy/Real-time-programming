package server

import akka.actor.{ActorSystem, Props}

import scala.io.StdIn

object MainServer {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()

    val port = 9010

    val server = system.actorOf(Props(new Server("lo", "ff02::2", port)), "Udp.Server")

    println(s"UDP Udp.Server up. Enter Ctl+C to stop...")

  }
}

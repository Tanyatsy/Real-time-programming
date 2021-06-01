package client

import akka.actor.{ActorSystem, Props}

import scala.io.StdIn

object MainClient {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()

    val portRemote = 9010
    val portLocal = 9600

    val client = system.actorOf(Props(new Client( "lo", "ff02::2", portRemote, portLocal)), "Udp.Client")

    StdIn.readLine() // run until user cancels

  }
}

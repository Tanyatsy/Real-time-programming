package client

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

import scala.io.StdIn

object MainClient {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()

    val portRemote = 9010
    val portLocal = 9600

    val client = system.actorOf(Props(new Client("lo", "ff02::2", portRemote, portLocal)), "Udp.Client")

    val clientInputListener = system.actorOf(Props(new ClientInputListener(client)), "clientInputListener")
    println("Please enter name of the topic")
    var msg = StdIn.readLine()
    while (!msg.equals("end")) {
      clientInputListener ! msg
      msg = StdIn.readLine()
    }
  }
}
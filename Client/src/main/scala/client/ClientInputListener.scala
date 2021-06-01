package client

import akka.actor.{Actor, ActorLogging, ActorRef}

class ClientInputListener(client: ActorRef) extends Actor with ActorLogging {
  def receive: Receive = {
    case msg: String =>
      client ! msg
  }
}

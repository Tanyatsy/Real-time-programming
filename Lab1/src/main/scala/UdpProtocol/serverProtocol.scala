package UdpProtocol

import akka.actor.ActorRef

import java.net.InetSocketAddress

object serverProtocol {
  var serverMessage = "Server msg: "
  case class Receive(msg: String, path: ActorRef)
  case class sendTweetTopic(msg: String)
  case class sendUserTopic(msg: String)
}

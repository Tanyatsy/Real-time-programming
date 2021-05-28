package UdpProtocol

import akka.actor.ActorRef

object serverProtocol {
  var serverMessage = "Server msg: "
  case class Receive(msg: String, path: ActorRef)
}

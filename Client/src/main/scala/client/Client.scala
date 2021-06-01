package client

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.{IO, Udp}
import akka.util.ByteString

import java.net.InetSocketAddress

class Client(iface: String, group: String, portRemote: Int, portLocal: Int) extends Actor with ActorLogging {

  import context.system

  val remote = new InetSocketAddress(s"$group%$iface", portRemote)
  IO(key = Udp) ! Udp.Bind(self, new InetSocketAddress(portLocal))

  def receive: Receive = {
    case Udp.Bound(_) =>
      context.become(ready(sender))
  }

  def ready(socket: ActorRef): Receive = {
    case msg: String =>
      socket ! Udp.Send(ByteString(msg, "UTF-8"), remote)
    case Udp.Received(data, _) =>
      println(s"Udp.Client received: ${data.decodeString("UTF-8")}")
    case Udp.Unbind => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }
}
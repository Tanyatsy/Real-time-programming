package ActorModel

import ActorModel.workerProtocol.{TweetTopic, UserTopic}
import UdpProtocol.serverProtocol.Inet6ProtocolFamily
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection}
import akka.io.{IO, Udp}
import akka.util.ByteString

import java.net.InetSocketAddress

class TopicSender(iface: String, group: String, port: Int) extends Actor with ActorLogging {

  import context.system

  val remote = new InetSocketAddress(s"$group%$iface", 9010)
  IO(key = Udp) ! Udp.Bind(self, new InetSocketAddress(9000))

  def receive = {
    case Udp.Bound(_) => {
      context.become(ready(sender))
    }
  }

  def ready(socket: ActorRef): Receive = {
    case TweetTopic(msg) =>
      self ! msg

    case UserTopic(msg) =>
      self ! msg

    case msg: String =>
      socket ! Udp.Send(ByteString(msg, "UTF-8"), remote)

    case Udp.Received(data, _) =>
      println(s"Udp.Client received: ${data.decodeString("UTF-8")}")

    case Udp.Unbind => socket ! Udp.Unbind

    case Udp.Unbound => context.stop(self)
  }

}

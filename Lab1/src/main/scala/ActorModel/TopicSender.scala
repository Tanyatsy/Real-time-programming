package ActorModel

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}
import akka.util.ByteString

import java.net.InetSocketAddress
import ActorModel.workerProtocol.{TweetTopic, UserTopic}

import ActorModel.workerProtocol.{TweetTopic, UserTopic}
import akka.actor.{Actor, ActorLogging, ActorRef}
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

    case Udp.Unbind => socket ! Udp.Unbind

    case Udp.Unbound => context.stop(self)
  }

}

/*
class TopicSender(local: InetSocketAddress, remote: InetSocketAddress) extends Actor {
  import context.system
  IO(key = Udp) ! Udp.Bind(self, local)

  def receive = {
    case Udp.Bound(_) =>
      context.become(ready(sender))
  }

  def ready(socket: ActorRef): Receive = {
    case TweetTopic(value)=>
        socket ! Udp.Send(ByteString(value, "UTF-8"), remote)
    case UserTopic(value)=>
        socket ! Udp.Send(ByteString(value, "UTF-8"), remote)

    case Udp.Received(data, _) =>
      println(s"Client received ${data.decodeString("UTF-8")}")
    case Udp.Unbind => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }
}
*/

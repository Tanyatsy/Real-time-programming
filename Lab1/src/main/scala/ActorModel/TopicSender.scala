package ActorModel

import ActorModel.workerProtocol.{TweetTopic, UserTopic}
import akka.actor.{Actor, ActorLogging, ActorSelection}
import akka.io.{IO, Udp}
import _root_.UdpProtocol.Inet6ProtocolFamily
import akka.util.ByteString

import java.net.InetSocketAddress

class TopicSender(iface: String, group: String, port: Int) extends Actor with ActorLogging {

  import context.system

  var tweet: String = new String
  var user: String = new String


  def receive = {
    case TweetTopic(msg) =>
      tweet = msg
      IO(Udp) ! Udp.SimpleSender(List(Inet6ProtocolFamily()))
      self ! Udp.SimpleSenderReady

    case UserTopic(msg) =>
      user = msg
      IO(Udp) ! Udp.SimpleSender(List(Inet6ProtocolFamily()))
      self ! Udp.SimpleSenderReady

    case Udp.SimpleSenderReady => {
      val remote = new InetSocketAddress(s"$group%$iface", port)
     // sender() ! Udp.Send(ByteString("Hiiii"), remote)
      if (tweet != null && tweet.nonEmpty) {
        log.info("Sending tweet topic to {}", remote)
        sender() ! Udp.Send(ByteString(tweet), remote)
      }
      if (user != null && user.nonEmpty) {
        log.info("Sending user topic to {}", remote)
        sender() ! Udp.Send(ByteString(user), remote)
      }
    }
    case Udp.Received(data, _) =>
      println(s"Client received ${data.decodeString("UTF-8")}")
  }
}

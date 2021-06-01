package server

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.{IO, Udp}
import akka.util.ByteString
import server.ServerProtocol.{Inet6ProtocolFamily, MulticastGroup}

import java.net.InetSocketAddress
import scala.collection.mutable.ListBuffer

class Server(iface: String, group: String, port: Int) extends Actor with ActorLogging {

  import context.system

  val opts = List(Inet6ProtocolFamily(), MulticastGroup(group, iface))
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(port), opts)

  var tweetsSubscribers: ListBuffer[InetSocketAddress] = ListBuffer[InetSocketAddress]()
  var usersSubscribers: ListBuffer[InetSocketAddress] = ListBuffer[InetSocketAddress]()


  def receive = {
    case Udp.Bound(local) =>
      log.info(s"UDP Server is listening to ${local.getAddress}:${local.getPort}")
      context.become(ready(sender))
  }

  def ready(sender: ActorRef): Receive = {

    case Udp.Received(data, remote) =>
      val msg = data.decodeString("utf-8").replaceAll("\n", " ")

      if (msg.equals("TweetsTopic")) {
        tweetsSubscribers += remote
        log.info(s"Subscriber: ${remote.getHostString}:${remote.getPort} says: ${msg}")
        sender ! Udp.Send(ByteString(s"You have successfully subscribed to the Topic: ${msg}"), remote)

      } else if (msg.equals("UsersTopic")) {
        usersSubscribers += remote
        log.info(s"Subscriber: ${remote.getHostString}:${remote.getPort} says: ${msg}")
        sender ! Udp.Send(ByteString(s"You have successfully subscribed to the Topic: ${msg}"), remote)

      } else if (msg.contains("TweetsTrue")) {
        tweetsSubscribers.foreach(subscriber => {
          sender ! Udp.Send(ByteString(msg), subscriber)
        })

      } else if (msg.contains("UsersTrue")) {
        usersSubscribers.foreach(subscriber => {
          sender ! Udp.Send(ByteString(msg), subscriber)
        })

      } else if (msg.equals("TweetsTopic ---")) {
        tweetsSubscribers.foreach(subscriber => {
          if (subscriber == remote) {
            tweetsSubscribers -= remote
            sender ! Udp.Send(ByteString(s"You have successfully unsubscribed from the Topic: Tweets Topic"), remote)
          }
        })

      } else if (msg.equals("UsersTopic ---")) {
        usersSubscribers.foreach(subscriber => {
          if (subscriber == remote) {
            usersSubscribers -= remote
            sender ! Udp.Send(ByteString(s"You have successfully unsubscribed from the Topic: Users Topic"), remote)
          }
        })


        /* case Udp.Unbind =>
           sender ! Udp.Unbind

         case Udp.Unbound =>
           context.stop(self)*/
      }
  }
}
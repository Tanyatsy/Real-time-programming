package UdpProtocol.server

import UdpProtocol.ServerProtocol._
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.io.{IO, Udp}
import akka.util.ByteString

import java.net._
import scala.collection.mutable.ListBuffer
import scala.io.StdIn

class Server(iface: String, group: String, port: Int) extends Actor with ActorLogging {

  import context.system

  val opts = List(Inet6ProtocolFamily(), MulticastGroup(group, iface))
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(port), opts)

  var tweetsSubscribers: ListBuffer[InetSocketAddress] = ListBuffer[InetSocketAddress]()
  var usersSubscribers: ListBuffer[InetSocketAddress] = ListBuffer[InetSocketAddress]()


  def receive = {
    case Udp.Bound(local) =>
      log.info(s"UDP Server is listening to ${local.getHostString}:${local.getPort}")
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

      } else if (msg.contains("tweetId")) {
        tweetsSubscribers.foreach(subscriber => {
          sender ! Udp.Send(ByteString(msg), subscriber)
        })

      } else if (msg.contains("tweetUserFull_name")) {
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

  class Listener(iface: String, group: String, port: Int, server: ActorRef) extends Actor with ActorLogging {

    import context.system

    val opts = List(Inet6ProtocolFamily(), MulticastGroup(group, iface))
    IO(Udp) ! Udp.Bind(self, new InetSocketAddress(port), opts)

    def receive = {
      case b@Udp.Bound(to) =>
        log.info("Bound to {}", to)
        sender ! b

      case Udp.Received(data, remote) =>
        val msg = data.decodeString("utf-8")
        if (!msg.contains(serverMessage)) {
          log.info("Received '{}' from {}", msg, sender())
          server ! Receive(msg, sender())
        }
    }
  }

  object ServerMain {

    def main(args: Array[String]): Unit = {
      implicit val system: ActorSystem = ActorSystem()

      val port = 9010

      val server = system.actorOf(Props(new Server("lo", "ff02::2", port)), "Udp.Server")
      //val listener = system.actorOf(Props(new Listener("lo", "ff02::2", port, server)), "Udp.Listener")

      println(s"UDP Udp.Server up. Enter Ctl+C to stop...")
      StdIn.readLine() // run until user cancels

    }
  }

package UdpProtocol

import UdpProtocol.serverProtocol.{Receive, sendTweetTopic, sendUserTopic, serverMessage}
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.io.Inet.{DatagramChannelCreator, SocketOptionV2}
import akka.io.{IO, Udp}
import akka.util.ByteString

import java.net._
import java.nio.channels.DatagramChannel
import scala.collection.mutable.ListBuffer
import scala.io.StdIn

final case class Inet6ProtocolFamily() extends DatagramChannelCreator {
  override def create() =
    DatagramChannel.open(StandardProtocolFamily.INET6)
}

//#multicast-group
final case class MulticastGroup(address: String, interface: String) extends SocketOptionV2 {
  override def afterBind(s: DatagramSocket): Unit = {
    val group = InetAddress.getByName(address)
    val networkInterface = NetworkInterface.getByName(interface)
    s.getChannel.join(group, networkInterface)
  }
}

class Server(iface: String, group: String, port: Int, msg: String) extends Actor with ActorLogging {

  import context.system

  val opts = List(Inet6ProtocolFamily(), MulticastGroup(group, iface))
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(port), opts)

  var tweetsSubscribers: ListBuffer[InetSocketAddress] = ListBuffer[InetSocketAddress]()
  var usersSubscribers: ListBuffer[InetSocketAddress] = ListBuffer[InetSocketAddress]()

  def receive = {

    case sendTweetTopic(msg) ⇒
      tweetsSubscribers.foreach( subscriber => {
        sender ! Udp.Send(ByteString(msg), subscriber)
        tweetsSubscribers -= subscriber
      })

    case sendUserTopic(msg) ⇒
      usersSubscribers.foreach( subscriber => {
        sender ! Udp.Send(ByteString(msg), subscriber)
        usersSubscribers -= subscriber
      })

    case Udp.Bound(local) =>
      log.info(s"UDP Server is listening to ${local.getHostString}:${local.getPort}")

    case Udp.Received(data, remote) =>
      val msg = data.decodeString("utf-8").replaceAll("\n", " ")

      log.info(s"Subscriber: ${remote.getHostString}:${remote.getPort} says: ${msg}")

      if(msg.equals("TweetsTopic")){
        tweetsSubscribers  += remote
      }else if(msg.equals("UsersTopic")){
        usersSubscribers += remote
      }else if(msg.contains("tweets")){
        self ! sendTweetTopic(msg)
      }else if(msg.contains("users")){
        self ! sendUserTopic(msg)
      }

      // echo back to sender
      sender ! Udp.Send(ByteString(s"You have successfully subscribed to Topic: ${msg}"), remote)

    case Udp.Unbind =>
      sender ! Udp.Unbind

    case Udp.Unbound =>
      context.stop(self)
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
      if(!msg.contains(serverMessage)){
        log.info("Received '{}' from {}", msg, sender())
        server ! Receive(msg, sender())
      }
  }
}

object ServerMain {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()

    val port = 9010

     val server = system.actorOf(Props(new Server("lo", "ff02::2", port, "HELLO")), "Udp.Server")
    //val listener = system.actorOf(Props(new Listener("lo", "ff02::2", port, server)), "Udp.Listener")

    println(s"UDP Udp.Server up. Enter Ctl+C to stop...")
    StdIn.readLine() // run until user cancels

  }
}
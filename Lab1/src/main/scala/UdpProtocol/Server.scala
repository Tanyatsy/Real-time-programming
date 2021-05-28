package UdpProtocol

import UdpProtocol.serverProtocol.{Receive, serverMessage}
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.io.Inet.{DatagramChannelCreator, SocketOptionV2}
import akka.io.{IO, Udp}
import akka.util.ByteString

import java.net._
import java.nio.channels.DatagramChannel
import scala.io.StdIn

final case class Inet6ProtocolFamily() extends DatagramChannelCreator {
  override def create() =
    DatagramChannel.open(StandardProtocolFamily.INET6)
}

//#inet6-protocol-family

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

  IO(Udp) ! Udp.SimpleSender(List(Inet6ProtocolFamily()))


  def receive = {
    case Udp.SimpleSenderReady => {
      val remote = new InetSocketAddress(s"$group%$iface", port)
      log.info("Sending message to {}", remote)
      sender() ! Udp.Send(ByteString(serverMessage + msg), remote)
    }
    case Receive(msg, sender) => {
      log.info("Received from listener {}", msg)
      IO(Udp) ! Udp.SimpleSender(List(Inet6ProtocolFamily()))
      val remote = new InetSocketAddress(s"$group%$iface", port)
      log.info("Sending message to {}", remote)
      //sender ! Udp.Send(ByteString(serverMessage + msg), remote)
      self ! Udp.SimpleSenderReady
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
    val listener = system.actorOf(Props(new Listener("lo", "ff02::2", port, server)), "Udp.Listener")

    println(s"UDP Udp.Server up. Enter Ctl+C to stop...")
    StdIn.readLine() // run until user cancels

  }
}
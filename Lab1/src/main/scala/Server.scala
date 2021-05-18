import java.net.InetSocketAddress

import java.net.{InetAddress, InetSocketAddress, NetworkInterface, StandardProtocolFamily}
import java.net.DatagramSocket
import java.nio.channels.DatagramChannel

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Inet.{DatagramChannelCreator, SocketOptionV2}
import akka.io.{IO, Udp}
import akka.util.ByteString
import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.io.Inet.DatagramChannelCreator
import akka.io.{IO, Udp}

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

class Sender(iface: String, group: String, port: Int, msg: String) extends Actor with ActorLogging {

  import context.system

  IO(Udp) ! Udp.SimpleSender(List(Inet6ProtocolFamily()))

  def receive = {
    case Udp.SimpleSenderReady => {
      val remote = new InetSocketAddress(s"$group%$iface", port)
      log.info("Sending message to {}", remote)
      sender() ! Udp.Send(ByteString(msg), remote)
    }
  }
}

class Listener(iface: String, group: String, port: Int, sink: ActorRef) extends Actor with ActorLogging {
  //#bind

  import context.system

  val opts = List(Inet6ProtocolFamily(), MulticastGroup(group, iface))
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(port), opts)
  //#bind

  def receive = {
    case b@Udp.Bound(to) =>
      log.info("Bound to {}", to)
      sink ! (b)
    case Udp.Received(data, remote) =>
      val msg = data.decodeString("utf-8")
      log.info("Received '{}' from {}", msg, remote)
      sink ! msg
  }
}

class UdpServer(host: String = "localhost", port: Int = 0) extends Actor {

  import context.system

  val log = Logging(context.system, this.getClass)

  override def preStart(): Unit = {
    log.info(s"Starting UDP Server on $host:$port")
    IO(Udp) ! Udp.Bind(self, new InetSocketAddress(port))
  }

  def receive: Receive = {
    case Udp.Bound(local) =>
      log.info(s"UDP Server is listening to ${local.getHostString}:${local.getPort}")

    case Udp.Received(data, remote) =>
      val msg = data.decodeString("utf-8").replaceAll("\n", " ")
      log.info(s"${remote.getHostString}:${remote.getPort} says: ${msg}")

      // echo back to sender
      sender ! Udp.Send(data, remote)

    case Udp.Unbind =>
      sender ! Udp.Unbind

    case Udp.Unbound =>
      context.stop(self)
  }


  override def postStop(): Unit = {
    log.info(s"Stopping UDP Server.")
  }

}

object Server {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()

    val host = "localhost"
    val port = 9010
    //  val udpServer = system.actorOf(Props(classOf[UdpServer], host, port))

    val sender = system.actorOf(Props(new Sender("lo", "ff02::2", port, "HELLO")), "Sender")
    val listener = system.actorOf(Props(new Listener("lo", "ff02::2", port, sender)), "Listener")

    println(s"UDP Server up. Enter Ctl+C to stop...")
    StdIn.readLine() // run until user cancels

  }
}
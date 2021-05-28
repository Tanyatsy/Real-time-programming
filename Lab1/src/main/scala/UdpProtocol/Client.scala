package UdpProtocol

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.io.{IO, Udp}
import akka.util.ByteString

import java.net.InetSocketAddress
import scala.io.StdIn

class Client(iface: String, group: String, port: Int) extends Actor with ActorLogging {

  import context.system
  val remote = new InetSocketAddress(s"$group%$iface", port)
  val opts = List(Inet6ProtocolFamily(), MulticastGroup(group, iface))
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(port), opts)

  def receive = {
    case Udp.Bound(_) =>
      context.become(ready(sender))
  }

  def ready(socket: ActorRef): Receive = {
    case msg: String =>
      socket ! Udp.Send(ByteString(msg, "UTF-8"), remote)
    case Udp.Received(data, _) =>
      println(s"Udp.Client received ${data.decodeString("UTF-8")}")
    case Udp.Unbind => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }
}

object Client {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()

    val port = 9010

    val client = system.actorOf(Props(new Client( "lo", "ff02::2", port)), "Udp.Client")

    StdIn.readLine() // run until user cancels

  }
}



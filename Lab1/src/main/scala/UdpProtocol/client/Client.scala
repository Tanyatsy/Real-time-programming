package UdpProtocol.client

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.io.{IO, Udp}
import akka.util.ByteString

import java.net.InetSocketAddress
import scala.io.StdIn

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

class ClientInputListener(client: ActorRef) extends Actor with ActorLogging {
  def receive: Receive = {
    case msg: String =>
      client ! msg
  }
}

object Client {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()

    val portRemote = 9010
    val portLocal = 9600

    val client = system.actorOf(Props(new Client("lo", "ff02::2", portRemote, portLocal)), "Udp.Client")

    val clientInputListener = system.actorOf(Props(new ClientInputListener(client)), "clientInputListener")
    println("Please enter name of the topic")
    var msg = StdIn.readLine()
    while (!msg.equals("end")) {
      clientInputListener ! msg
      msg = StdIn.readLine()
    }
  }
}



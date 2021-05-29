package UdpProtocol

import akka.actor.ActorRef
import akka.io.Inet.{DatagramChannelCreator, SocketOptionV2}

import java.net.{DatagramSocket, InetAddress, InetSocketAddress, NetworkInterface, StandardProtocolFamily}
import java.nio.channels.DatagramChannel

object serverProtocol {
  var serverMessage = "Server msg: "
  case class Receive(msg: String, path: ActorRef)
  case class sendTweetTopic(msg: String)
  case class sendUserTopic(msg: String)

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

}


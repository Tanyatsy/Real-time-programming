import akka.actor.{ActorRef, ActorSystem, Props}

import scala.sys.process._

object Main {


  def main(args: Array[String]) = {
   /* val system = ActorSystem("main")
    val mongoDBActor = system.actorOf(Props[MongoDBConnector], "mongo")*/
   val system = ActorSystem("main")
    val router = system.actorOf(Props[RouterActor], "router")
    val ws = system.actorOf(Props(new WorkerSupervisor(router)), name = "workerSupervisor")
    val ws_2 = system.actorOf(Props(new WorkerSupervisor_2(router)), name = "workerSupervisor_2")
    val aggregator = system.actorOf(Props[AggregatorActor], "aggregator")
    val connector = system.actorOf(Props(new ConnectorActor(router)), name = "connector")
    connector ! "send"

    //in case when we need to restart docker container
   /* runCommand()
    def runCommand() {
      val command = Seq("docker", "restart", "46a88e10abbd")
      val os = sys.props("os.name").toLowerCase
      val panderToWindows = os match {
        case x if x contains "windows" => Seq("cmd", "/C") ++ command
        case _ => command
      }
      panderToWindows.!
    }*/
  }
}

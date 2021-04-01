import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, ActorSystem, OneForOneStrategy, Props}
import workerProtocol.{RestartException, WorkersPool_2}

import scala.collection.mutable.ListBuffer

class WorkerSupervisor_2(router: ActorRef) extends Actor with ActorLogging {

  val system: ActorSystem = context.system
  var numberOfWorkers = 5

  var workers_2: ListBuffer[ActorPath] = ListBuffer()
  for (i <- 1 to numberOfWorkers) {
    val myWorkingActor = context.actorOf(Props[WorkerActor_2](), name = "worker_" + i)
    workers_2 :+= myWorkingActor.path
  }
  router ! WorkersPool_2(workers_2)

  override def supervisorStrategy: OneForOneStrategy = OneForOneStrategy() {
    case _: RestartException => Restart
  }

  override def receive: Receive = {
    case message =>
      print("")
  }
}

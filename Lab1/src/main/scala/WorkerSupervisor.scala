import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, ActorSystem, OneForOneStrategy, Props}
import workerProtocol.{RestartException, WorkersPool, throwException}

import scala.Console._
import scala.collection.mutable.ListBuffer


class WorkerSupervisor(router: ActorRef) extends Actor with ActorLogging {

  val system: ActorSystem = context.system
  var numberOfWorkers = 5

  var workers: ListBuffer[ActorPath] = ListBuffer()
  for (i <- 1 to numberOfWorkers) {
    val myWorkingActor = context.actorOf(Props[WorkerActor](), name = "worker" + i)
    workers :+= myWorkingActor.path
  }
  router ! WorkersPool(workers)

  override def supervisorStrategy: OneForOneStrategy = OneForOneStrategy() {
    case _: RestartException => Restart
  }

  override def receive: Receive = {
    case throwException(error, workerPath) =>
      log.info(s"${RED}There is an error: " + error + " from " + workerPath + s"${RESET}")

    case messagesCount: Int => {
      if (messagesCount < 2) {
        workers.remove(numberOfWorkers - 1)
        numberOfWorkers = numberOfWorkers - 1
      }
      else if (messagesCount > 50) {
        val myWorkingActor = context.actorOf(Props[WorkerActor](), name = "worker" + (numberOfWorkers + 1))
        numberOfWorkers = numberOfWorkers + 1
        workers.addOne(myWorkingActor.path)
      }
      router ! workers
    }
  }
}
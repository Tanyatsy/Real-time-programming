import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, ActorSystem, Props}
import workerProtocol.Work

import scala.collection.mutable.ListBuffer

class RouterActor extends Actor with ActorLogging {

  var rooterWorkers: ListBuffer[ActorPath] = ListBuffer()
  var currentIndex = 0

  override def receive: Receive = {
    case workers: ListBuffer[ActorPath] =>
      rooterWorkers = workers

    case temp: Work =>
      context.system.actorSelection(rooterWorkers(RoundRobinLogic(rooterWorkers))) ! (temp)
  }


  def RoundRobinLogic(list: ListBuffer[ActorPath]): Int = {
    currentIndex = currentIndex + 1
    if (currentIndex == list.length) {
      currentIndex = 0
    }
    currentIndex
  }

}

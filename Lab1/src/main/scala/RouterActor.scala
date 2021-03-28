import akka.actor.{Actor, ActorLogging, ActorPath}
import workerProtocol.{Work, WorkersPool, WorkersPool_2}

import scala.collection.mutable.ListBuffer

class RouterActor extends Actor with ActorLogging {

  var rooterWorkers: ListBuffer[ActorPath] = ListBuffer()
  var rooterWorkers_2: ListBuffer[ActorPath] = ListBuffer()
  var currentIndex = 0
  var currentIndex_2 = 0

  override def receive: Receive = {
    case WorkersPool(workers) =>
      rooterWorkers = workers

    case WorkersPool_2(workers) =>
      rooterWorkers_2 = workers

    case temp: Work =>
      if(rooterWorkers.nonEmpty)
      context.system.actorSelection(rooterWorkers(RoundRobinLogic(rooterWorkers))) ! (temp)
      if(rooterWorkers_2.nonEmpty)
      context.system.actorSelection(rooterWorkers_2(RoundRobinLogic_2(rooterWorkers_2))) ! (temp)
  }


  def RoundRobinLogic(list: ListBuffer[ActorPath]): Int = {
    currentIndex = currentIndex + 1
    if (currentIndex == list.length) {
      currentIndex = 0
    }
    currentIndex
  }

  def RoundRobinLogic_2(list: ListBuffer[ActorPath]): Int = {
    currentIndex_2 = currentIndex_2 + 1
    if (currentIndex_2 == list.length) {
      currentIndex_2 = 0
    }
    currentIndex_2
  }

}

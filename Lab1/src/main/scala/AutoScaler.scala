import akka.actor.{Actor, ActorLogging, ActorSelection}

import java.util.Calendar
import scala.collection.mutable;

class AutoScaler extends Actor with ActorLogging {
  private val stack = mutable.Stack[Long]()
  var interval = 1000
  var ws: ActorSelection = context.system.actorSelection("user/workerSupervisor")

  def receive(): Receive = {
    case timeStamp: Long =>
      stack.push(timeStamp)
      countMessages()
  }

  def countMessages(): Unit = {
    var messagesCount = 0
    var index = stack.length
    var isMatchToInterval = Calendar.getInstance().getTime.getTime - stack.top < interval
    while (index > 0 && isMatchToInterval) {
      index -= 1
      messagesCount += 1
      isMatchToInterval = Calendar.getInstance().getTime.getTime - stack(stack.length - index - 1) < interval
    }
    ws ! messagesCount
  }

}
 
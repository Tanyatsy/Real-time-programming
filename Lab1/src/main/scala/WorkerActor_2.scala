import akka.actor.{Actor, ActorLogging, ActorSelection}
import workerProtocol.{RestartException, SendEngagement, Work, throwException}

import scala.Console.{RESET, YELLOW}


class WorkerActor_2 extends Actor with ActorLogging {

  var ws_2: ActorSelection = context.system.actorSelection("user/workerSupervisor_2")
  var aggregator: ActorSelection = context.system.actorSelection("user/aggregator")
  var router: ActorSelection = context.system.actorSelection("user/router")


  override def receive: Receive = {
    case Work(temp, id) =>
      if (temp.contains(": panic")) {
        ws_2 ! throwException(temp, self.path)
        throw new RestartException
      }
      else if (ujson.read(temp)("message")("tweet").obj.contains("retweeted_status")) {

        val s = "{\"message\":{\"tweet\":" + ujson.read(temp)("message")("tweet")("retweeted_status").toString + "}}"
        router ! Work(s, id)
      }
      else {
        val favourites_count: Int = ujson.read(temp)("message")("tweet")("user")("favourites_count").toString.toInt

        val followers_count: Int = ujson.read(temp)("message")("tweet")("user")("followers_count").toString.toInt

        val retweet_count: Int = ujson.read(temp)("message")("tweet")("retweet_count").toString.toInt

        val engagement_ratio: Int = if (followers_count != 0) (favourites_count + retweet_count) / followers_count else 0

        log.info("--------------------------------")
        log.info(s"Engagement_ratio: " + s"${YELLOW}" + engagement_ratio + s"${RESET}")

        aggregator ! SendEngagement(engagement_ratio, id)
      }
  }
}

import akka.actor.{Actor, ActorLogging, ActorSelection}
import workerProtocol.{RestartException, SendTweetScore, Work, throwException}

import scala.Console._
import scala.io.Source
import scala.util.Random

class WorkerActor extends Actor with ActorLogging {

  var ws: ActorSelection = context.system.actorSelection("user/workerSupervisor")
  var aggregator: ActorSelection = context.system.actorSelection("user/aggregator")

  override def receive: Receive = {
    case Work(temp, id) =>
      Thread.sleep(Random.nextInt(450) + 50)

      if (temp.contains(": panic")) {

        ws ! throwException(temp, self.path)
        throw new RestartException

      } else {

        val messageText: String = ujson.read(temp)("message")("tweet")("text").value.toString
        val tweetsWords: Array[String] = messageText.split(" ")
        val emotions: Map[String, List[String]] = readTextFile("emotion_values.txt")
        var tweetScore = 0
        var tweetEmotions = ""

        tweetsWords.foreach(word => {
          for ((k, v) <- emotions) {
            if (word.equals(k)) {
              tweetEmotions = k + " "
              tweetScore = tweetScore + v.head.toInt
            }
          }
        })

        // log.info("Message: " + messageText)

        if (!tweetEmotions.equals("")) {
          log.info("--------------------------------")
          log.info(s"Tweet emotions word: " + s"${CYAN}" + tweetEmotions + s"${RESET}")
          aggregator ! SendTweetScore(tweetScore, id)
        }
        log.info("--------------------------------")
        log.info(s"Tweet score: " + s"${MAGENTA}" + tweetScore.toString + s"${RESET}")
        // log.info(s"my actorRef:${self.path.name}")
      }
  }

  def readTextFile(filename: String): Map[String, List[String]] = {
    val emotions =
      for {
        line <- Source.fromFile(filename).getLines()
        split = line.split("\t").map(_.trim).toList
        emotion = split.head
        scores = split.tail
      } yield (emotion -> scores)
    emotions.toMap
  }

}


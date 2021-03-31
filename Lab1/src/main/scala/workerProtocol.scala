import akka.actor.ActorPath

import scala.collection.mutable.ListBuffer

object workerProtocol{
  case class Work(text:String, id: String)
  class RestartException extends Exception("RESTART")
  case class throwException(errorName:String, actorPath: ActorPath)
  case class WorkersPool(value: ListBuffer[ActorPath])
  case class WorkersPool_2(value: ListBuffer[ActorPath])
  case class SendEngagement(value: Int, id: String)
  case class SendTweetScore(value: Int, id: String)
  case class Tweets(tweets: List[Tweet])
  case class Result(result: String)
}
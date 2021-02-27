import akka.actor.ActorPath

object workerProtocol{
  case class Work(name:String)
  class RestartException extends Exception("RESTART")
  case class throwException(errorName:String, actorPath: ActorPath)
}
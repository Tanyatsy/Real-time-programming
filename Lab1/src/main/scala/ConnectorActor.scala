import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.ThrottleMode
import akka.stream.alpakka.sse.scaladsl.EventSource
import akka.stream.scaladsl.{Sink, Source}
import workerProtocol.Work

import java.util.Calendar
import java.util.UUID.randomUUID
import scala.collection.immutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}


class ConnectorActor(router: ActorRef) extends Actor {

  implicit val system: ActorSystem = context.system
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val autoScaler: ActorRef = system.actorOf(Props[AutoScaler], "scaler")


  def receive(): Receive = {
    case "send" =>

      val send: HttpRequest => Future[HttpResponse] = Http().singleRequest(_)

      val eventSource: Source[ServerSentEvent, NotUsed] =
        EventSource(
          uri = Uri(s"http://localhost:4000/tweets/1"),
          send,
          initialLastEventId = None,
          retryDelay = 1.second
        )

      val eventSource2: Source[ServerSentEvent, NotUsed] =
        EventSource(
          uri = Uri(s"http://localhost:4000/tweets/2"),
          send,
          initialLastEventId = None,
          retryDelay = 1.second
        )

      executeEvents(eventSource, eventSource2)
  }

  def executeEvents(eventSource: Source[ServerSentEvent, NotUsed], eventSource2: Source[ServerSentEvent, NotUsed]): Unit = {
    val eventsList: List[Source[ServerSentEvent, NotUsed]] = List(eventSource, eventSource2)

    val list: List[Future[immutable.Seq[ServerSentEvent]]] = eventsList.toStream.map(sse => {
      sse.throttle(elements = 1, per = 500.milliseconds, maximumBurst = 1, ThrottleMode.Shaping)
        .take(20)
        .runWith(Sink.seq)
    }).toList

    //parallel execution
    val v = Vector(list.head, list.last)
    v.foreach(x => {
      x.foreach(serverEvent =>
        serverEvent.foreach(
          data => {
            val temp = data.getData()
            val id = randomUUID().toString
            router ! Work(temp, id)
            autoScaler ! getCurrentMinute
          }
        )
      )
    })

    while (!list.head.isCompleted || !list.last.isCompleted) {}

    executeEvents(eventSource, eventSource2)
  }


  def getCurrentMinute: Long = {
    val dT = Calendar.getInstance()
    dT.getTime.getTime
  }

}
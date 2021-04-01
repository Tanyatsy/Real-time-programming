import Helpers.GenericObservable
import akka.actor.{Actor, ActorSystem}
import org.mongodb.scala.{Document, _}
import play.api.libs.json.{JsObject, JsString}
import workerProtocol.Tweets

import java.util.{Timer, TimerTask}
import scala.collection.mutable.ListBuffer


class MongoDBConnector extends Actor {

  implicit val system: ActorSystem = context.system
  val mongoClient: MongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase("RTP")


  var maximumBatchSize: Int = 128
  var tweetsJson: ListBuffer[String] = ListBuffer[String]()
  var usersJson: ListBuffer[String] = ListBuffer[String]()

  //  val observable: SingleObservable[Document] = collection.find().first()
  //  observable.subscribe((doc: Document) => println(doc.toJson()))

  def receive: Receive = {
    case Tweets(tweets) => {
      sender() ! "received"

      tweets.foreach(tweet => {
        val tweetSeq = Seq(
          "tweetId" -> JsString(tweet.tweetId),
          "tweet" -> JsString(tweet.tweetMessage),
          "tweetScore" -> JsString(tweet.tweetsScore.toString),
          "tweetEngagement" -> JsString(tweet.tweetsEngagement.toString)
        )
        tweetsJson += JsObject(tweetSeq).toString()
      })

      adaptiveBatchingForTweets()

      tweets.foreach(tweet => {
        val UserSeq = Seq(
          "_id" -> JsString(tweet.tweetId),
          "tweetUserScreen_name" -> JsString(tweet.tweetUser),
          "tweetUserFull_name" -> JsString(tweet.tweetUserName)
        )

        usersJson += JsObject(UserSeq).toString()
      })

      adaptiveBatchingForUsers()

    }
  }

  def insertUsersToDB(): Unit = {

    val collection: MongoCollection[Document] = database.getCollection("Users")
    val documents = generateDbDoc(usersJson)
    collection.insertMany(documents).results()

  }

  def insertTweetsToDB(): Unit = {

    /*  val doc = Document(tweetsJson(0))

      val observable: SingleObservable[Completed] = collection.insertOne(doc)

      observable.subscribe(new Observer[Completed] {

        override def onNext(result: Completed): Unit = println("Inserted")

        override def onError(e: Throwable): Unit = println("Failed")

        override def onComplete(): Unit = println("Completed")
      })*/

    val collection: MongoCollection[Document] = database.getCollection("Tweets")
    val documents = generateDbDoc(tweetsJson)
    collection.insertMany(documents).results()

  }

  def generateDbDoc(arr: ListBuffer[String]): IndexedSeq[Document] = convertToSeq(arr) map { value: String => Document(value) }

  def convertToSeq(arr: ListBuffer[String]): IndexedSeq[String] = for (i <- arr.indices) yield arr(i)

  def adaptiveBatchingForUsers(): Unit = {
    if (usersJson.size >= maximumBatchSize) {
      insertUsersToDB()
      usersJson = ListBuffer[String]()
    }
    else {
      val trigger = new Timer()
      trigger.scheduleAtFixedRate(new TimerTask {
        def run() = {
          insertUsersToDB()
          usersJson = ListBuffer[String]()
          self ! true
          trigger.cancel()
        }
      }, 2000, 1)
    }
  }

  def adaptiveBatchingForTweets(): Unit = {
    if (tweetsJson.size >= maximumBatchSize) {
      insertTweetsToDB()
      tweetsJson = ListBuffer[String]()
    }
    else {
      val trigger = new Timer()
      trigger.scheduleAtFixedRate(new TimerTask {
        def run() = {
          insertTweetsToDB()
          tweetsJson = ListBuffer[String]()
          self ! true
          trigger.cancel()
        }
      }, 2000, 1)
    }
  }

}

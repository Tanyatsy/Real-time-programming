import akka.actor.{Actor, ActorLogging, ActorSelection}
import akka.pattern.ask
import akka.util.Timeout
import workerProtocol.{SendEngagement, SendTweetScore, Tweets, Work}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class AggregatorActor extends Actor with ActorLogging {

  var tweetsEngagementId: Map[String, Int] = Map()
  var tweetsScoreId: Map[String, Int] = Map()
  var tweetsDetails: Map[Map[Int, Int], String] = Map()
  var tweetMessage: Map[String, String] = Map()
  var tweets: ListBuffer[Tweet] = ListBuffer[Tweet]()

  var mongoConnector: ActorSelection = context.system.actorSelection("user/mongo")

  override def receive: Receive = {
    case SendEngagement(engagement, id) =>
      tweetsEngagementId += (id -> engagement)
      matchTweetsId()

    case SendTweetScore(tweetScore, id) =>
      tweetsScoreId += (id -> tweetScore)
      matchTweetsId()

    case Work(temp, id) =>
      tweetMessage += (id -> temp)

      if (tweets.nonEmpty) {
        var result = ""
        tweets.foreach(tweet => {
          tweetMessage.keys.foreach(currentTweetId => {
            if (tweet.tweetId.equals(currentTweetId)) {
              if (!tweetMessage(currentTweetId).contains(": panic")) {
                tweet.tweetMessage = ujson.read(tweetMessage(currentTweetId))("message")("tweet")("text").value.toString
                tweet.tweetUser = ujson.read(tweetMessage(currentTweetId))("message")("tweet")("user")("screen_name").value.toString
                tweet.tweetUserName = ujson.read(tweetMessage(currentTweetId))("message")("tweet")("user")("name").value.toString
              }
              implicit val timeout: Timeout = Timeout(2 seconds)
              val future = mongoConnector ? Tweets(tweets.find(tweet => tweet.tweetMessage != null).toList)
              result = Await.result(future, timeout.duration).asInstanceOf[String]
              tweetMessage -= currentTweetId
            }
          }
          )
          if (result != null) {
            tweets -= tweet
          }
        })
      }
  }

  def matchTweetsId(): Unit = {
    tweetsEngagementId.keys.foreach(engagementId => {
      tweetsScoreId.keys.foreach({ tweetScoreId => {
        if (engagementId.equals(tweetScoreId)) {
          // tweetsDetails += (Map(tweetsScoreId(tweetScoreId) -> tweetsEngagementId(engagementId)) -> engagementId)
          var tweet: Tweet = new Tweet()
          tweet.tweetsEngagement = tweetsEngagementId(engagementId)
          tweet.tweetsScore = tweetsScoreId(tweetScoreId)
          tweet.tweetId = engagementId
          tweets.addOne(tweet)
          tweetsScoreId -= tweetScoreId
          tweetsEngagementId -= engagementId
        }
      }
      })
    })
  }

}

import akka.actor.{Actor, ActorLogging}
import workerProtocol.{SendEngagement, SendTweetScore, Work}

import scala.collection.mutable.ListBuffer

class AggregatorActor extends Actor with ActorLogging {

  var tweetsEngagementId: Map[String, Int] = Map()
  var tweetsScoreId: Map[String, Int] = Map()
  var tweetsDetails: Map[Map[Int, Int], String] = Map()
  var tweetMessage: Map[String, String] = Map()
  var tweets : ListBuffer[Tweet] = ListBuffer[Tweet]()

  override def receive: Receive = {
    case SendEngagement(engagement, id) =>
      tweetsEngagementId += (id -> engagement)
      matchTweetsId()

    case SendTweetScore(tweetScore, id) =>
      tweetsScoreId += (id -> tweetScore)
      matchTweetsId()

    case Work(temp, id) => {
      tweetMessage += (id -> temp)

      tweets.foreach(tweet => {
        tweetMessage.keys.foreach(currentTweetId => {
          if (tweet.tweetId.equals(currentTweetId)) {
            tweet.tweetMessage = tweetMessage(currentTweetId)
            print(tweet.toString)
            tweetMessage -= currentTweetId
          }
        }
        )
      })
    }
  }

  def matchTweetsId(): Unit = {
    tweetsEngagementId.keys.foreach(engagementId => {
      tweetsScoreId.keys.foreach({ tweetScoreId => {
        if (engagementId.equals(tweetScoreId)) {
          // tweetsDetails += (Map(tweetsScoreId(tweetScoreId) -> tweetsEngagementId(engagementId)) -> engagementId)
          var tweet : Tweet = new Tweet()
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

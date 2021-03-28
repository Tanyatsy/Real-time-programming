import akka.actor.{Actor, ActorSystem}

import org.mongodb.scala._




class MongoDBConnector extends Actor{

  implicit val system: ActorSystem = context.system
  val mongoClient: MongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase("RTP")

  val collection: MongoCollection[Document] = database.getCollection("Tweets")

  val observable: SingleObservable[Document] = collection.find().first()

  observable.subscribe((doc: Document) => println(doc.toJson()))


  override def preStart = {
  }

  def receive = {
    case _ =>
  }
}

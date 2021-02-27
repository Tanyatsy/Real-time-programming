# First lab at real-time programming(Streaming Twitter sentiment analysis system)
  
  ## Table of contents
  
  * [Task Description](#task-description)
  * [Implementation](#implementation)
     * [SSE](#sse) 
     * [Actors and Classes](#actors)
     * [Workers](#workers)
     * [AutoScaler](#autoscaler)
  * [Output example video](#output-example-video)
  * [How to use](#how-to-use)
  
## Task Description
In this project we should implement a streaming Twitter sentiment analysis system, which will read 2 SSE streams of actual Twitter API tweets in JSON format.
After we will receive the messages, we should route the messages to a group of workers that need to be autoscaled, we will need to scale up the workers (have more) when the rate is high, and less actors when the rate is low.
Our route/load balance messages among worker actors in a round robin fashion. Also we should kill/crash workers, when we will receive "kill messages".
We will have to have a supervisor/restart policy for the workers. The worker actors also must have a random sleep, in the range of 50ms to 500ms, normally distributed.

-------------------------

## Implementation 

I have the following features implemented in my project :
* Connector actor(makes HTTP requests and get recursively new events)
* Router actor(transfers data between workers)
* Worker actor(parses Json and calculate all needed data)
* Worker supervisor actor(generates workers depend on message count)
* Autoscaler(finds amount of messages per second)

#### SSE

Firstly, I created "ConnectorActor.scala" class which will contain all logic of server-sent events and data changing between workers.
I have used sse example from https://doc.akka.io/docs/alpakka/current/sse.html
Also I did logic for parallel execution of requests using scala.Vectors & scala.Feature

#### Actors 

All my classes extends Actor class, in order to create actor model, which provides a higher level of abstraction for writing concurrent systems.
Due this I can easily transfer messages between my actors.
Each actor implemented "receive" method, where we can define different cases for received messages from other actors.

#### Workers 

Workers are needed for system analyser. They analyzed every message, which is received from router.
In order to get a worker and send him a message I have used "actorSelection" method, due it all messages easily sends to workers using "Round Robin logic" when routing the messages to the workers.
 
In order to parse messages I used scala.UJson.
Also we might receive a "Panic message", so I have implemented case when we throw exception, when such panic-message is received and I restart worker, who has parsed this message.

#### AutoScaler

I have implemented "AutoScaler.scala" class, in order to find how much messages I receive per second. 
I have used a stack which contains time when the message was received, after this I compare each message time with current and scale the difference of it in order to calculate messages, difference of which is less then 1 second.
After this the number of messages send to "WorkerSupervisor.scala" class which depends on this count creates pool of workers with specific number. 


-------------------------

#### Output example video
![](https://github.com/Tanyatsy/Real-time-programming/Lab1/blob/master/src/main/resources/RTP_Lab1.gif?raw=true)

-------------------------

## How to use

- You need to install "Scala" plugin
- Run Main.object in order to execute program

## Author

* [**Tiguliova Tatiana**](https://github.com/Tanyatsy)

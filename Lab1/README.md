#Third lab at real-time programming
  
  ## Table of contents
  
  * [Task Description](#task-description)
  * [Implementation](#implementation)
  * [How to use](#how-to-use)
  
## Task Description
So, we have to implement a message broker and integrate it with the previous laboratory.

We have to develop a message broker with support for multiple topics(I have implemented: UsersTopic, TweetsTopic). 
The message broker should be a dedicated async TCP/UDP server written in the language of the previous 2 labs.(I have implemented: UDP) 
We must connect the second lab to it so that the lab will publish messages on the message broker which can be subscribed to using a tool like telnet or netcat.
We must implement separate docker images for docker-compose
Also, we have to do a more-or-less sound software design, where the messages in your code are represented as structures/classes and not as just maps or strings.
Of course, this implies we will need to serialize these messages at some point, to send them via network.
-------------------------

## Implementation 

I have the following features implemented in my project :

* Udp Protocol (Client/Server)
  
  I have used "import akka.io.{IO, Udp}"
  
  Also I implemented multi-client server with "Inet6ProtocolFamily()"
  
* Docker Compose
  
  I have used "docker compose up"
  
* Serialize logic 
  
  I have used "import play.api.libs.json.{JsObject, JsString}"


## How to Use
 
1) In order to run the program you need to run "docker compose up".

2) Also you should run the ClientMain Scala and subscribe to the TweetsTopic/UsersTopic.

3) Or you can run client via netcat "nc -u localhost 9010".

## Author

* [**Tiguliova Tatiana**](https://github.com/Tanyatsy)


FROM  openjdk:8u232

ENV SBT_VERSION 1.4.7
RUN \
   mkdir /working/ && \
   cd /working/ && \
   curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
   dpkg -i sbt-$SBT_VERSION.deb && \
   rm sbt-$SBT_VERSION.deb && \
   apt-get update && \
   apt-get install sbt && \
   cd && \
   rm -r /working/ && \
   sbt sbtVersion

# copy source code

WORKDIR /Lab1/src/main/scala/UdpProtocol/client

ADD . /Lab1/src/main/scala/UdpProtocol/client

RUN sbt run
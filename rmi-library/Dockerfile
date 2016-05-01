FROM ubuntu
RUN apt-get update && apt-get install -y default-jre default-jdk
COPY rmi /rmi
RUN javac -Xlint rmi/PingPongServer.java
RUN javac -Xlint rmi/PingPongClient.java

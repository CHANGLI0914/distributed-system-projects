FROM ubuntu
RUN apt-get update && apt-get install -y default-jre default-jdk
COPY rmi /rmi
RUN javac rmi/PingPongServer.java
RUN javac rmi/PingPongClient.java

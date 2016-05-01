#!/bin/sh

docker build -t javaenv:1.0 .
docker network create -d bridge mynetwork

docker run -itd --net=mynetwork --name server javaenv:1.0 java rmi/PingPongServer 7000
docker run -P -t --net=mynetwork --name client javaenv:1.0 java rmi/PingPongClient server 7000

echo "\nCLeaning...\n"
docker stop server
docker rm server
docker rm client
docker network rm mynetwork

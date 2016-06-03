#!/bin/bash

# Build Docker images
# docker build -t slaveimage:1.0 ./slave
# docker build -t masterimage:1.0 ./master
# docker network create -d bridge mynetwork

# Start 4 Slave Nodes
docker run -id --net=mynetwork --hostname slave1 --name slave1 slaveimage:1.0
docker run -id --net=mynetwork --hostname slave2 --name slave2 slaveimage:1.0
docker run -id --net=mynetwork --hostname slave3 --name slave3 slaveimage:1.0
docker run -id --net=mynetwork --hostname slave4 --name slave4 slaveimage:1.0

# Start Master Node
docker run -it --net=mynetwork --hostname master --name master \
                -v $(pwd)/mounted:/mounted \
                masterimage:1.0 /mounted/run.sh

# Clean
docker rm -f slave1
docker rm -f slave2
docker rm -f slave3
docker rm -f slave4
docker rm -f master

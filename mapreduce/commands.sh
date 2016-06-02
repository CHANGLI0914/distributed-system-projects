docker build -t slaveimage:1.0 ./slave
docker build -t masterimage:1.0 ./master
docker network create -d bridge mynetwork

docker run -id --net=mynetwork --hostname slave1 --name slave1 slaveimg
docker run -id --net=mynetwork --hostname slave2 --name slave2 slaveimg
docker run -id --net=mynetwork --hostname slave3 --name slave3 slaveimg
docker run -id --net=mynetwork --hostname slave4 --name slave4 slaveimg

docker run -it --net=mynetwork --hostname master --name master masterimg /etc/bootstrap.sh -bash

ipslave1=`docker exec slave1 /sbin/ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v inet6|grep -v 192.168.99|awk '{print $2}'|tr -d "addr:"`
ipslave2=`docker exec slave1 /sbin/ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v inet6|grep -v 192.168.99|awk '{print $2}'|tr -d "addr:"`
ipslave3=`docker exec slave1 /sbin/ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v inet6|grep -v 192.168.99|awk '{print $2}'|tr -d "addr:"`
ipslave4=`docker exec slave1 /sbin/ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v inet6|grep -v 192.168.99|awk '{print $2}'|tr -d "addr:"`

echo slave1 >>etc/hosts

#CMD ["/etc/bootstrap.sh", "-d"]

$HADOOP_PREFIX/bin/hadoop dfsadmin -report




export JAVA_HOME=/usr/java/default
export PATH=${JAVA_HOME}/bin:${PATH}
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar

bin/hadoop com.sun.tools.javac.Main WordCountBiagram.java
bin/hadoop com.sun.tools.javac.Main WordCountTopN.java
jar cf wc.jar WordCount*.class
bin/hadoop dfs -copyFromLocal wc.jar 


bin/hadoop fs -ls 



cat> file1
apple apple apple a and and apple apple he like I think I like apple apple 

bin/hadoop dfs -copyFromLocal file1

bin/hadoop dfs -copyFromLocal wc.jar 

bin/hadoop jar wc.jar WordCountNum file1 out1 3


bin/hadoop fs -cat out6/part-r-00000


cd $HADOOP_PREFIX


etc/hadoop/core-site.xml:

172.18.0.2	slave1
172.18.0.3	slave2
172.18.0.4	slave3
172.18.0.5	slave4
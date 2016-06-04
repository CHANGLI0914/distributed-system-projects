#!/bin/bash

# Init
/etc/bootstrap.sh
cd /mounted

# Set ENV
export JAVA_HOME=/usr/java/default
export PATH=${JAVA_HOME}/bin:${PATH}
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar

# Compile
$HADOOP_PREFIX/bin/hadoop com.sun.tools.javac.Main WordCountBiagram.java
$HADOOP_PREFIX/bin/hadoop com.sun.tools.javac.Main -Xlint WordCountTopN.java
$HADOOP_PREFIX/bin/hadoop com.sun.tools.javac.Main WordCountNum.java
$HADOOP_PREFIX/bin/hadoop com.sun.tools.javac.Main -Xlint WordCountPercent.java

# Make Archive
jar cf wc.jar *.class

# Copy files to the dfs
$HADOOP_PREFIX/bin/hadoop dfs -copyFromLocal wc.jar /
$HADOOP_PREFIX/bin/hadoop dfs -copyFromLocal input.txt /


# Run
$HADOOP_PREFIX/bin/hadoop jar wc.jar WordCountBiagram /input.txt /out_1
$HADOOP_PREFIX/bin/hadoop jar wc.jar WordCountTopN /input.txt /out_2 10
$HADOOP_PREFIX/bin/hadoop jar wc.jar WordCountNum /input.txt /out_3
$HADOOP_PREFIX/bin/hadoop jar wc.jar WordCountPercent /input.txt /out_4

# Cat results
echo "Result of WordCountBiagram:"
$HADOOP_PREFIX/bin/hadoop fs -cat /out_1/part-r-00000
echo "Result of WordCountTopN:"
$HADOOP_PREFIX/bin/hadoop fs -cat /out_2/part-r-00000
echo "Result of WordCountNum:"
$HADOOP_PREFIX/bin/hadoop fs -cat /out_3/part-r-00000
echo "Result of WordCountPercent:"
$HADOOP_PREFIX/bin/hadoop fs -cat /out_4/part-r-00000

# Clean
$HADOOP_PREFIX/bin/hadoop dfs -rm  /wc.jar
$HADOOP_PREFIX/bin/hadoop dfs -rm  /input.txt
$HADOOP_PREFIX/bin/hadoop dfs -rm  -r /out_1
$HADOOP_PREFIX/bin/hadoop dfs -rm  -r /out_3
rm wc.jar
rm *.class

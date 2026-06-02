#!/bin/sh
export PATH=$PATH:/usr/java/jdk1.17.0.1/bin
export JAVA_HOME="/usr/java/jdk-17.0.1/"
export MV_HOME="/usr/local/bin/maven-3.9.3"

"$MV_HOME"/bin/mvn -Denv=dev clean package -log-file build.txt
grep -e 'BUILD' build.txt

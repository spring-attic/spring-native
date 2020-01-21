#!/usr/bin/env sh

set -e

yum install -y unzip wget procps bc
wget https://repo1.maven.org/maven2/org/apache/maven/apache-maven/3.5.3/apache-maven-3.5.3-bin.zip
unzip apache-maven-3.5.3-bin.zip
export PATH=$PATH:`pwd`/apache-maven-3.5.3/bin

gu install native-image
cd spring-graal-native
./build-feature.sh
./build-key-samples.sh

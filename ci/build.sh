#!/usr/bin/env sh

set -e

npm install tty-table -g
yum install -y unzip wget procps bc perl util-linux 

go get github.com/fullstorydev/grpcurl
go install github.com/fullstorydev/grpcurl/cmd/grpcurl

wget https://repo1.maven.org/maven2/org/apache/maven/apache-maven/3.5.3/apache-maven-3.5.3-bin.zip
unzip apache-maven-3.5.3-bin.zip
export PATH=$PATH:`pwd`/apache-maven-3.5.3/bin

gu install native-image
cd spring-graal-native
./build-feature.sh
./build-key-samples.sh

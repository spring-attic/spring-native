#!/usr/bin/env sh

set -e

yum update -y oraclelinux-release-el7 \
    && yum install -y oraclelinux-developer-release-el7 oracle-softwarecollection-release-el7 \
    && yum-config-manager --enable ol7_developer \
    && yum-config-manager --enable ol7_developer_EPEL \
    && yum-config-manager --enable ol7_optional_latest \
    && yum install -y bzip2-devel ed gcc gcc-c++ gcc-gfortran gzip file fontconfig less libcurl-devel make openssl openssl-devel readline-devel tar vi which xz-devel zlib-devel \
    glibc-static libcxx libcxx-devel libstdc++-static zlib-static \
    unzip wget procps bc perl util-linux golang git

fc-cache -f -v
gu install native-image

npm install tty-table -g

go get github.com/fullstorydev/grpcurl
go install github.com/fullstorydev/grpcurl/cmd/grpcurl

wget https://repo1.maven.org/maven2/org/apache/maven/apache-maven/3.5.3/apache-maven-3.5.3-bin.zip
unzip apache-maven-3.5.3-bin.zip
export PATH=$PATH:`pwd`/apache-maven-3.5.3/bin:/root/go/bin:$JAVA_HOME/jre/languages/js/bin

cd spring-graal-native
./build-feature.sh
./build-key-samples.sh

#!/usr/bin/env sh

mkdir /tmp/data
/usr/bin/mongod --fork --dbpath /tmp/data --logpath /tmp/data/mongod.log
redis-server --daemonize yes
cd spring-graalvm-native
./build.sh
./build-samples.sh

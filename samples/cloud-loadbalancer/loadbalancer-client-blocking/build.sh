#!/usr/bin/env bash

# Build test-service
cd ../test-service
mvn clean package
cd ../loadbalancer-client-blocking

${PWD%/*samples/*}/scripts/compileWithMaven.sh $* || exit 1

java -jar ../test-service/target/test-service-0.0.1-SNAPSHOT.jar &
SERVERPID=$!
sleep 10

 ${PWD%/*samples/*}/scripts/test.sh $*

kill ${SERVERPID}


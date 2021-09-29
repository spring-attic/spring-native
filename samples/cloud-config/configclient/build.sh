#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

# Build the server
cd ../configserver
./build.sh
./target/configserver 2>&1 > target/native/server-output.txt &
SERVERPID=$!

cd ../configclient

${PWD%/*samples/*}/scripts/compileWithMaven.sh $* || exit 1

sleep 10

 ${PWD%/*samples/*}/scripts/test.sh $*

kill ${SERVERPID}


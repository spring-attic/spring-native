#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

# Build the server
cd ../configserver
./build.sh
cd ../configclient

${PWD%/*samples/*}/scripts/compileWithMaven.sh || exit 1

../configserver/target/configserver 2>&1 > target/native/server-output.txt &
SERVERPID=$!
sleep 10 

${PWD%/*samples/*}/scripts/test.sh

kill ${SERVERPID}


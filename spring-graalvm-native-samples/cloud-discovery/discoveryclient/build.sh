#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

# Build the server
cd ../discoveryserver
mvn clean package
cd ../discoveryclient

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"
./compile.sh || exit 1

java -jar ../discoveryserver/target/discoveryserver-0.0.1-SNAPSHOT.jar &
SERVERPID=$!
sleep 10 

${PWD%/*samples/*}/scripts/test.sh

kill ${SERVERPID}


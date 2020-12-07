#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

# Build the server
cd ../configserver
./build.sh
cd ../configclient-new

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"
./compile.sh || exit 1

#java -jar ../configserver/target/configserver-0.0.1-SNAPSHOT.jar &
../configserver/target/configserver &
SERVERPID=$!
sleep 10 

${PWD%/*samples/*}/scripts/test.sh

kill ${SERVERPID}


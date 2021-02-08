#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

# Build the server
cd ../configserver
${PWD%/*samples/*}/scripts/compileWithMaven.sh || exit 1
cd ../configclient

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"
${PWD%/*samples/*}/scripts/compileWithMaven.sh || exit 1

# Run the JDK one or the compiled image one!
#java -jar ../configserver/target/configserver-0.0.1-SNAPSHOT.jar &
../configserver/target/configserver 2>&1 > target/native-image/server-output.txt &

SERVERPID=$!
sleep 10 

${PWD%/*samples/*}/scripts/test.sh

kill ${SERVERPID}


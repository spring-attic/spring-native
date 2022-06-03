#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh

BLUE='\033[0;34m'
NC='\033[0m'

if [[ "$1" == "--aot-only" ]] || [[ "$1" == "-a" ]]; then
  AOT_ONLY=true
else
  AOT_ONLY=false
fi

# Build the server
cd ../configserver
./build.sh $*
if [[ "$AOT_ONLY" == true ]]; then
  java -Dspring.aot.enabled=true -jar target/*.jar 2>&1 > target/native/server-output.txt &
else
  ./target/configserver 2>&1 > target/native/server-output.txt &
fi
SERVERPID=$!

wait_log target/native/server-output.txt "Started ConfigServerApplication"

cd ../configclient

${PWD%/*samples/*}/scripts/compileWithMaven.sh $* || exit 1

${PWD%/*samples/*}/scripts/test.sh $*

if ps -p $SERVERPID > /dev/null; then
  kill $SERVERPID
fi


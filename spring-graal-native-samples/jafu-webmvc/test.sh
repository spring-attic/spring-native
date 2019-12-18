#!/usr/bin/env bash

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

EXECUTABLE=${PWD##*/}
echo "Testing $EXECUTABLE output"

./target/${EXECUTABLE} > target/native-image/test-output.txt 2>&1 &
PID=$!
sleep 1

RSS=`ps -o rss ${PID} | tail -n1`
RSS=`bc <<< "scale=1; ${RSS}/1024"`
echo "RSS memory: ${RSS}M"
SIZE=`wc -c <"./target/${EXECUTABLE}"`/1024
SIZE=`bc <<< "scale=1; ${SIZE}/1024"`
echo "Image size: ${SIZE}M"
STARTUP=`cat target/native-image/test-output.txt | grep "JVM running for"`
REGEXP="Started .* in (.*)\$"
if [[ ${STARTUP} =~ ${REGEXP} ]]; then
	echo "Startup time: ${BASH_REMATCH[1]}"
fi

RESPONSE=`curl -s localhost:8080/`
if [[ "$RESPONSE" == 'Hello' ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
  kill ${PID}
  exit 0
else
  printf "${RED}FAILURE${NC}: the output of the application does not contain the expected output\n"
  kill ${PID}
  exit 1
fi
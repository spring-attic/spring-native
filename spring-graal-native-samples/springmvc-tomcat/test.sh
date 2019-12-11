#!/usr/bin/env bash

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "Testing springmvc-tomcat output"

./target/springmvc-tomcat >> target/native-image/output.txt 2>&1 &
SERVER_PID=$!
sleep 1
RESPONSE=`curl -s localhost:8080/`
if [[ "$RESPONSE" == 'Hello from tomcat' ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
  kill $SERVER_PID
  exit 0
else
  printf "${RED}FAILURE${NC}: the output of the application does not contain the expected output\n"
  kill $SERVER_PID
  exit 1
fi
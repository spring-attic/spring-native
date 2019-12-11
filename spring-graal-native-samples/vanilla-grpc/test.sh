#!/usr/bin/env bash

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "Testing vanilla-grpc"

./target/vanilla-grpc >> target/native-image/output.txt 2>&1 &
SERVER_PID=$!
sleep 1

OUTPUT=`grpcurl -plaintext localhost:50051 describe demo.Greeter 2>&1`
if [[ ! $OUTPUT == *"demo.Greeter is a service:"* ]]
then
  printf "${RED}FAILURE${NC}: the output of the application does not contain the expected output\n"
  kill $SERVER_PID
  exit 1
fi

OUTPUT=`grpcurl -plaintext -d '{}' localhost:50051 demo.Greeter/Hello 2>&1`
if [[ $OUTPUT == *"\"firstName\": \"Josh\","* ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
  kill $SERVER_PID
  exit 0
else
  printf "${RED}FAILURE${NC}: the output of the application does not contain the expected output\n"
  kill $SERVER_PID
  exit 1
fi
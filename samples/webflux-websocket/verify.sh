#!/usr/bin/env bash
curl -s localhost:8080/ws/greetings
if [[ `cat target/native/test-output.txt | grep -F "Started WebfluxWebsocketApplication" | grep -F -v "500 Server Error"` ]]; then
  exit 0
else
  exit 1
fi
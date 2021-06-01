#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/`
if [[ `cat target/native/test-output.txt | grep -E "Started WebsocketApplication"` ]]; then
  exit 0
else
  exit 1
fi

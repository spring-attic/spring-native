#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/actuator/health`
if [[ "$RESPONSE" == *"UP"* ]]; then
  exit 0
else
  exit 1
fi

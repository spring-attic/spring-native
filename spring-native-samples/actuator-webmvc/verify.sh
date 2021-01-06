#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/actuator/health`
if [[ "$RESPONSE" == *"UP"* ]]; then
  exit 0
else 
  echo "failed to get UP health response"
  exit 1
fi

#!/usr/bin/env bash
RESPONSE=`curl -I localhost:8080/`
if [[ "$RESPONSE" == *"HTTP/1.1 401"* ]]; then
  exit 0
else
  exit 1
fi

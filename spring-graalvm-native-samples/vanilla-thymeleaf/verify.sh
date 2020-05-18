#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/greeting`
if [[ "$RESPONSE" == *"Hello, World!"* && "$RESPONSE" == *"<title>"* ]]; then
  exit 0
else
  exit 1
fi

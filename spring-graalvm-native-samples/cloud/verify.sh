#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/`
if [[ "$RESPONSE" == 'hi!' ]]; then
  RESPONSE2=`curl -s localhost:8080/actuator`
  if [[ $RESPONSE2 == "{"* ]]; then
        exit 0
  else
        exit 1
  fi
else
  exit 1
fi

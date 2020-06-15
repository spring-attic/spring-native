#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/`
if [[ "$RESPONSE" == 'hi!' ]]; then
  RESPONSE2=`curl -s localhost:8080/x`
  if [[ "$RESPONSE2" == 'hix!' ]]; then
        exit 0
  else
        exit 1
  fi
else
  exit 1
fi

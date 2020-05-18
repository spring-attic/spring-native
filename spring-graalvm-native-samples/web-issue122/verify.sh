#!/usr/bin/env bash
RESPONSE=`curl localhost:8080/API`
if [[ "$RESPONSE" == '{"name":"hello there."}' ]]; then
  RESPONSE2=`curl localhost:8080/API/`
  if [[ "$RESPONSE2" == '{"name":"hi there."}' ]]; then
    exit 0
  fi
fi
exit 1

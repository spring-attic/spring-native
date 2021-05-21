#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/with`
if [[ "$RESPONSE" != 'hello1' ]]; then
  echo $RESPONSE
  exit 1
fi
RESPONSE=`curl -s localhost:8080/without`
if [[ "$RESPONSE" != 'hello2' ]]; then
  echo $RESPONSE
  exit 2
fi
exit 0


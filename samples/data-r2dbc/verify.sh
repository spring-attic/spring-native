#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/reservations`
if [[ "$RESPONSE" == '[{"id":1,"name":"Andy"},{"id":2,"name":"Sebastien"}]' ]]; then
  exit 0
else
  exit 1
fi

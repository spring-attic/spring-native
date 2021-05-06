#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/reservations`
if [[ "$RESPONSE" == '[{"name":"Andy","id":1},{"name":"Sebastien","id":2}]' ]]; then
  exit 0
else
  exit 1
fi

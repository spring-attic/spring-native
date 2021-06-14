#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/managers/1/employees`
if [[ "$RESPONSE" == *"Bilbo"* ]]; then
  exit 0
else
  exit 1
fi


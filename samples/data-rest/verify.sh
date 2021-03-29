#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/api/customers/1`
if [[ "$RESPONSE" == *"Matthews"* ]]; then
  exit 0
else
  exit 1
fi


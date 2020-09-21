#!/usr/bin/env bash

RESPONSE=`curl -v -d world http://localhost:9001/2015-03-31/functions/foobar/invocations`
if [[ "$RESPONSE" == '"hi world!"' ]]; then
  exit 0
else
  exit 1
fi

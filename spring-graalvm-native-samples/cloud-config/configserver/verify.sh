#!/usr/bin/env bash
RESPONSE=`curl localhost:9000/config-client/dev/master | jq -r .label`
if [[  "$RESPONSE" == "master" ]]; then
  exit 0
else
  exit 1
fi

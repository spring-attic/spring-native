#!/usr/bin/env bash
RESPONSE=`./target/validator -Ddelay=0 -Dapp.value=Last 2>&1 | grep "Valid: \[\]"`
if [[ "$RESPONSE" == 'Valid: []' ]]; then
  RESPONSE2=`./target/validator -Ddelay=0 -Dapp.value=123 2>&1 | grep "Reason: Invalid lastname"`
  if [[ "$RESPONSE2" == *"Reason: Invalid lastname"* ]]; then
    exit 0
  else
    echo "RESPONSE2 for valid run is $RESPONSE2"
    exit 1
  fi
else
  echo "RESPONSE for valid run is $RESPONSE"
  exit 1
fi

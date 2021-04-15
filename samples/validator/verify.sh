#!/usr/bin/env bash
RESPONSE=`./target/validator -Dapp.value=123 2>&1 | grep "Reason: Invalid lastname"`
if [[ "$RESPONSE" == *"Reason: Invalid lastname"* ]]; then
    RESPONSE2=`curl -d 'testIntMin=-1' -s localhost:8080/validateForm`
    if [[ "$RESPONSE2" == 'Validation failed: -1' ]]; then
      RESPONSE3=`curl -d 'testIntMin=1' -s localhost:8080/validateForm`
      if [[ "$RESPONSE3" == 'Validation passed: 1' ]]; then
        exit 0
      else
        echo "Response for the web form with testIntMin: -1 is invalid: $RESPONSE2"
        exit 1
      fi
      exit 0
    else
      echo "Response for the web form with testIntMin: -1 is invalid: $RESPONSE2"
      exit 1
    fi
  else
    echo "Response for app.value=123 is invalid: $RESPONSE"
    exit 1
  fi
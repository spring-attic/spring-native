#!/usr/bin/env bash
if [[ ! `cat target/native-image/test-output.txt | grep -E "Application run failed|No suitable logging system located"` ]]
then
  exit 0
else 
  RESPONSE=`curl -s localhost:8080/actuator/health`
  if [[ "$RESPONSE" == *"UP"* ]]; then
   exit 0
  else
   exit 1
  fi
fi

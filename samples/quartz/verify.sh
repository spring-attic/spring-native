#!/usr/bin/env bash
sleep 15
RESPONSE=`grep World target/native/test-output.txt | wc -l`
if [[ "$RESPONSE" == *"6"* ]]; then
  exit 0
else
  exit 1
fi

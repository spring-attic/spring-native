#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/actuator/health`
if [[ "$RESPONSE" != *"UP"* ]]; then
  echo "Failed to get UP response: $RESPONSE"
  exit 1
fi
RESPONSE=`curl -s localhost:8080/actuator/prometheus | grep ^jvm_classes_loaded_classes | wc -l`
if [[ "$RESPONSE" != *"1"* ]]; then
  echo "Failed to get data from prometheus endpoint: $RESPONSE"
  exit 1
fi
exit 0

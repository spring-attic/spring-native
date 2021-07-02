#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/actuator/health`
if [[ "$RESPONSE" != *"UP"* ]]; then
  echo "Failed to get UP response: $RESPONSE"
  exit 1
fi
RESPONSE=`curl -s localhost:8080/actuator/prometheus | grep ^jvm_classes_loaded_classes | wc -l`
if [[ "$RESPONSE" != *"1"* ]]; then
  echo "Failed to get data from prometheus endpoint: $RESPONSE"
  exit 2
fi
RESPONSE=`curl -s localhost:8080/actuator/info`
if [[ "$RESPONSE" != *"{}"* ]]; then
  echo "Failed to get data from info endpoint: $RESPONSE"
  exit 3
fi
RESPONSE=`curl -s localhost:8080/actuator/metrics`
if [[ "$RESPONSE" != *"jvm.classes.loaded"* ]]; then
  echo "Failed to get data from metrics endpoint: $RESPONSE"
  exit 4
fi
RESPONSE=`curl -s localhost:8080/actuator/metrics/jvm.classes.loaded`
if [[ "$RESPONSE" != *"statistic"* ]]; then
  echo "Failed to get data from metrics/jvm.classes.loaded endpoint: $RESPONSE"
  exit 5
fi
RESPONSE=`curl -s localhost:8080/actuator/custom`
if [[ "$RESPONSE" != *"OK"* ]]; then
  echo "Failed to get data from custom endpoint: $RESPONSE"
  exit 6
fi
RESPONSE=`curl -s localhost:8080/actuator/customnc`
if [[ "$RESPONSE" != *"OK"* ]]; then
  echo "Failed to get data from customnc endpoint: $RESPONSE"
  exit 7
fi
exit 0

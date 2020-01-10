#!/usr/bin/env bash
OUTPUT=`cat target/native-image/test-output.txt`
if [[ ${OUTPUT} == *"INFO: info"* && ${OUTPUT} == *"SEVERE: ouch"* && ${OUTPUT} == *"java.lang.RuntimeException"* ]]
then
  exit 0
else 
  exit 1
fi

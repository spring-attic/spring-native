#!/usr/bin/env bash
OUTPUT=`cat target/native-image/test-output.txt`
if [[ ${OUTPUT} == *"INFO com.example.logger.LoggerApplication - info"* && ${OUTPUT} == *"ERROR com.example.logger.LoggerApplication - ouch"* && ${OUTPUT} == *"java.lang.RuntimeException"* ]]
then
  exit 0
else 
  exit 1
fi

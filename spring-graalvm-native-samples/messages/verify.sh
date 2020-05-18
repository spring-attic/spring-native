#!/usr/bin/env bash
OUTPUT=`cat target/native-image/test-output.txt`
if [[ ${OUTPUT} == *"Welcome"* && ${OUTPUT} == *"true"* ]]
then
  exit 0
else 
  exit 1
fi

#!/usr/bin/env bash
if [[ `cat target/native-image/test-output.txt | grep ">> This was run in a Spring Batch app!"` ]]; then
  exit 0
else
  exit 1
fi

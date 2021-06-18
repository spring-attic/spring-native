#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "set:Asynchronous action running..."` ]]; then
  exit 0
fi
exit 1

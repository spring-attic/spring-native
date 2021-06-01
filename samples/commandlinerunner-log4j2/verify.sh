#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "ERROR log message"` ]]; then
  exit 0
else
  exit 1
fi

#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "The number is now 6"` ]]; then
  exit 0
else
  exit 1
fi

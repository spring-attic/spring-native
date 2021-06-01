#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "Batch IO ran!"` ]]; then
  exit 0
else
  exit 1
fi

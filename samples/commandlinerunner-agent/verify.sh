#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "commandlinerunner running!"` ]]; then
  exit 0
else
  exit 1
fi

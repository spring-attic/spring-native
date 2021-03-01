#!/usr/bin/env bash
if [[ `cat build/native-image/test-output.txt | grep "commandlinerunner running!"` ]]; then
  exit 0
else
  exit 1
fi

#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "commandlinerunner running!"` ]]; then
  if [[ `cat target/native/test-output.txt | grep "ApplicationContextAware callback invoked"` ]]; then
    exit 0
  else
    exit 1
  fi
else
  exit 1
fi

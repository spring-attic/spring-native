#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "Hello, Apache Logging world!"` ]]; then
  if [[ `cat target/native/test-output.txt | grep "Hello, Slf4j world!"` ]]; then
    exit 0
  else
    exit 1
  fi
else
  exit 1
fi

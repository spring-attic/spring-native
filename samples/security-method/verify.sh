#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "Started MethodSecurityApplication"` ]]; then
  exit 0
else
  exit 1
fi

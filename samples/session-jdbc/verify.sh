#!/usr/bin/env bash
if [[ `cat target/native-image/test-output.txt | grep "Started SessionJdbcApplication"` ]]; then
  exit 0
else
  exit 1
fi

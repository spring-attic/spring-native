#!/usr/bin/env bash
if [[  `cat target/native/test-output.txt | grep -E "from application-dev.yml"` ]]; then
  exit 0
else
  exit 1
fi

#!/usr/bin/env bash
if [[  `cat target/native/test-output.txt | grep -E "from application-ci.yml"` ]]; then
  exit 0
else
  exit 1
fi

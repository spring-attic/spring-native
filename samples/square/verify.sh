#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "https://api.github.com/users/joshlong"` ]]; then
  exit 0
else
  exit 1
fi

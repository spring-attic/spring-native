#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "The number is now 6"` ]]; then
  if [[ `cat target/native/test-output.txt | grep "The other number is now 6"` ]]; then
    exit 0
  fi
fi
exit 1

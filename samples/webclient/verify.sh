#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "the Speed Force gives him plenty of abilities"` ]]; then
  exit 0
else
  exit 1
fi

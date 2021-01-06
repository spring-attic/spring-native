#!/usr/bin/env bash
if [[ `cat target/native-image/test-output.txt | grep "bootfeatures running andy:secret"` ]]; then
  exit 0
else
  exit 1
fi

#!/usr/bin/env bash
if [[ `cat target/native-image/test-output.txt | grep -E "Modified: 4"` ]]
then
  exit 0
else 
  exit 1
fi

#!/usr/bin/env bash
sleep 6
if [[ `cat target/native/test-output.txt | grep -E "DONE"` ]]
then
  exit 0
else 
  exit 1
fi

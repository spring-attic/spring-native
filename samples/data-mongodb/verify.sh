#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep -E "QUERY BY EXAMPLE"` ]]
then
  exit 0
else 
  exit 1
fi

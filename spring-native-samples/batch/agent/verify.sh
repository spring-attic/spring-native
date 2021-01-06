#!/usr/bin/env bash
if [[ `cat test-output.txt | grep "Batch ran!"` ]]; then
  exit 0
else
  exit 1
fi

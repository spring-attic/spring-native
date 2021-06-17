#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "EL: Received hello event: andy"` ]]; then
  if [[ `cat target/native/test-output.txt | grep "TEL: Received hello event: andy"` ]]; then
    if [[ `cat target/native/test-output.txt | grep "EL: Received hello event: sebastien"` ]]; then
	    exit 0
    fi
  fi
fi
exit 1

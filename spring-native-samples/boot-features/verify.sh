#!/usr/bin/env bash
if [[ `cat target/native-image/test-output.txt | grep "bootfeatures running andy:secret"` ]]; then
  if [[ `cat target/native-image/test-output.txt | grep "school is School\[Steve P, Dave H\]"` ]]; then
    if [[ `cat target/native-image/test-output.txt | grep "uni is University\[Andy C, Brian B\]"` ]]; then
      if [[ `cat target/native-image/test-output.txt | grep "acme is false null andy secret  \[USER\]"` ]]; then
        exit 0
      fi
    fi
  fi
fi
exit 1

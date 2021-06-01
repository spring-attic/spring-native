#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "bootfeatures running andy:secret"` ]]; then
  if [[ `cat target/native/test-output.txt | grep "school is School\[Steve P, Dave H\]"` ]]; then
    if [[ `cat target/native/test-output.txt | grep "uni is University\[Andy C, Brian B\]"` ]]; then
      if [[ `cat target/native/test-output.txt | grep "acme is false null andy secret  \[USER\]"` ]]; then
        if [[ `cat target/native/test-output.txt | grep "props is red class path resource \[foo.txt\]"` ]]; then
          exit 0
        fi
      fi
    fi
  fi
fi
exit 1

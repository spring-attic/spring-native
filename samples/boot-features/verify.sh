#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_log target/native/test-output.txt "bootfeatures running andy:secret"' ERR
trap 'wait_log target/native/test-output.txt "school is School\[Steve P, Dave H\]"' ERR
trap 'wait_log target/native/test-output.txt "uni is University\[Andy C, Brian B\]"' ERR
trap 'wait_log target/native/test-output.txt "acme is false null andy secret  \[USER\]"' ERR
wait_log target/native/test-output.txt "props is red class path resource \[foo.txt\]"


#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "bootfeatures running andy:secret" || RC=$?
wait_log target/native/test-output.txt "school is School\[Steve P, Dave H\]" || RC=$?
wait_log target/native/test-output.txt "uni is University\[Andy C, Brian B\]" || RC=$?
wait_log target/native/test-output.txt "acme is false null andy secret  \[USER\]" || RC=$?
wait_log target/native/test-output.txt "props is red class path resource \[foo.txt\]" || RC=$?

exit $RC

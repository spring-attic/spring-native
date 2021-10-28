#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "Data{url='http://httpbin.org/anything'" || RC=$?
wait_log target/native/test-output.txt "Data{url='https://httpbin.org/anything'" || RC=$?

exit $RC

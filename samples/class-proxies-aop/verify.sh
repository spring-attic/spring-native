#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "methodA: A-from-aspect" || RC=$?
wait_log target/native/test-output.txt "methodB: B" || RC=$?

exit $RC

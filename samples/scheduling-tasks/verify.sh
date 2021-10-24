#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "The number is now 6" || RC=$?
wait_log target/native/test-output.txt "The other number is now 6" || RC=$?

exit $RC

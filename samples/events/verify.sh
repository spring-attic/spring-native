#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "EL: Received hello event: andy" || RC=$?
wait_log target/native/test-output.txt "EL: Received hello event: sebastien" || RC=$?

exit $RC

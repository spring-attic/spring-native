#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "commandlinerunner running!" || RC=$?
wait_log target/native/test-output.txt "ApplicationContextAware callback invoked" || RC=$?

exit $RC

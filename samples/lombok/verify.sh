#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "Hello, Apache Logging world!" || RC=$?
wait_log target/native/test-output.txt "Hello, Slf4j world!" || RC=$?

exit $RC

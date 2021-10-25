#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "the Speed Force gives him plenty of abilities" || RC=$?
wait_log target/native/test-output.txt '"url": "https://httpbin.org/anything"' || RC=$?

exit $RC

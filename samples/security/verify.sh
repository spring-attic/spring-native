#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "Started SecuringWebApplication" || RC=$?
wait_http localhost:8080/home "Welcome" || RC=$?
wait_command_output 'curl -I localhost:8080/hello' "HTTP/1.1 302" || RC=$?

exit $RC

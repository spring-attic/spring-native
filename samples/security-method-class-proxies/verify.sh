#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "Started MethodSecurityApplication" || RC=$?
wait_http localhost:8080/hello "Unauthorized" || RC=$?
wait_http user:password@localhost:8080/hello "Hello!" || RC=$?
wait_http admin:password@localhost:8080/admin/hello "Goodbye!" || RC=$?

exit $RC

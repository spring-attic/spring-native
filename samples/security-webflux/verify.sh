#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "Started SecurityWebfluxApplication" || RC=$?
wait_command_output 'curl -I  localhost:8080/hello' "Unauthorized" || RC=$?
wait_http user:password@localhost:8080/hello "Hello!" || RC=$?
wait_command_output 'curl -I user:password@localhost:8080/admin' "Forbidden" || RC=$?
wait_http admin:password@localhost:8080/admin "Welcome to the administrator page!" || RC=$?

exit $RC

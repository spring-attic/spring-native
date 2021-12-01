#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "Started SecurityLdapApplication" || RC=$?
wait_command_output 'curl -I  localhost:8080/' "401" || RC=$?
wait_http user:password@localhost:8080/ "Hello, user!" || RC=$?
wait_http user:password@localhost:8080/friendly "Hello, Dianne Emu!" || RC=$?

exit $RC
#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "Started SecurityWebfluxApplication" || RC=$?
wait_http localhost:8080/rest/anonymous "anonymous" || RC=$?
wait_command_output 'curl -s -u user:password localhost:8080/rest/authorized' "authorized: user" || RC=$?
wait_command_output 'curl -s -u admin:password localhost:8080/rest/admin' "admin: admin" || RC=$?
wait_command_output 'curl -s -I localhost:8080/rest/authorized' "HTTP/1.1 401" || RC=$?
wait_command_output 'curl -s -I localhost:8080/rest/admin' "HTTP/1.1 401" || RC=$?
wait_command_output 'curl -s -I -u user:password localhost:8080/rest/admin' "HTTP/1.1 403" || RC=$?

exit $RC

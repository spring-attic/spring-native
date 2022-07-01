#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "Started MethodSecurityApplication" || RC=$?
wait_command_output "curl -I localhost:8080/hello" "HTTP/1.1 401" || RC=$?
wait_command_output "curl -I -u user:password localhost:8080/admin/hello" "HTTP/1.1 403" || RC=$?
wait_http user:password@localhost:8080/hello "Hello!" || RC=$?
wait_http admin:password@localhost:8080/admin/hello "Goodbye!" || RC=$?

exit $RC

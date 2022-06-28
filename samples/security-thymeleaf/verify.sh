#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_log target/native/test-output.txt "Started SecurityThymeleafApplication"
wait_http localhost:8080/ "Welcome!" || RC=$?
wait_http user:password@localhost:8080/hello "Hello <span>user</span>" || RC=$?
wait_command_output 'curl -s -I localhost:8080/hello' "HTTP/1.1 401" || RC=$?

exit $RC

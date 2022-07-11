#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_http localhost:8080/greeting "Form" || RC=$?
wait_command_output 'curl -d "id=0" -d "content=test" -X POST http://localhost:8080/greeting' "content: test" || RC=$?

exit $RC

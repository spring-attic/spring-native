#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_http localhost:8080/greeting "Hello, World!" || RC=$?
wait_http localhost:8080/greeting "<title>" || RC=$?
wait_http localhost:8080/greetings "<td>foo</td>" || RC=$?
wait_http localhost:8080/greetings "<td>bar</td>" || RC=$?

exit $RC

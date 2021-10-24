#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_http localhost:8080 "hi!" || RC=$?
wait_http localhost:8080/actuator "{" || RC=$?

exit $RC

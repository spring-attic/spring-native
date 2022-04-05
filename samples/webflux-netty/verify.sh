#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_http localhost:8080/ "hi!" || RC=$?
wait_http localhost:8080/x "hix!" || RC=$?
wait_http localhost:8080/hello "World" || RC=$?

exit $RC

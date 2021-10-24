#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_http localhost:8080/with "hello1" || RC=$?
wait_http localhost:8080/without "hello2" || RC=$?

exit $RC

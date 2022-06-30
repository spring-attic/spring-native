#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_http localhost:8080/ "hi!" || RC=$?
wait_http localhost:8080/x "hix!" || RC=$?
wait_http localhost:8080/hello "World" || RC=$?
wait_http localhost:8080/record '{"field1":"Hello","field2":"World"}' || RC=$?

exit $RC

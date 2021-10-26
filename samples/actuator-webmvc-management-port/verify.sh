#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "Started ActuatorApplication in" || RC=$?
wait_http localhost:8080/hello "Hello World" || RC=$?
wait_http localhost:8081/actuator/health "UP" || RC=$?

exit $RC

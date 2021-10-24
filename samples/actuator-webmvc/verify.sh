#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "Started ActuatorApplication in" || RC=$?
wait_http localhost:8080/actuator/health/readiness "customIndicator" || RC=$?
wait_http localhost:8080/actuator/health "UP" || RC=$?

exit $RC

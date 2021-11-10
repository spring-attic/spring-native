#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "Started DemoApplication in" || RC=$?
wait_http localhost:8080/actuator/health "UP" || RC=$?
wait_http localhost:8080/actuator/prometheus "jvm_classes_loaded_classes" || RC=$?
wait_http localhost:8080/actuator/info "{}" || RC=$?
wait_http localhost:8080/actuator/metrics "jvm.classes.loaded" || RC=$?
wait_http localhost:8080/actuator/metrics/jvm.classes.loaded "statistic" || RC=$?
wait_http localhost:8080/actuator/caches "{}" || RC=$?
wait_http localhost:8080/actuator/custom "OK" || RC=$?
wait_http localhost:8080/actuator/customnc "OK" || RC=$?

exit $RC

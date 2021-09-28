#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_log target/native/test-output.txt "Started DemoApplication in"' ERR
trap 'wait_http localhost:8080/actuator/health "UP"' ERR
trap 'wait_http localhost:8080/actuator/prometheus "jvm_classes_loaded_classes"' ERR
trap 'wait_http localhost:8080/actuator/info "{}"' ERR
trap 'wait_http localhost:8080/actuator/metrics "jvm.classes.loaded"' ERR
trap 'wait_http localhost:8080/actuator/metrics/jvm.classes.loaded "statistic"' ERR
trap 'wait_http localhost:8080/actuator/custom "OK"' ERR
wait_http localhost:8080/actuator/customnc "OK"

#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_log target/native/test-output.txt "Started ActuatorApplication in"' ERR
wait_http localhost:8080/actuator/health "UP"

#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_http localhost:8080 "hi!"' ERR
wait_http localhost:8080/actuator "{"

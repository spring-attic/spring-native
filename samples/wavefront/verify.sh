#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_http localhost:8080 "Hello from tomcat"

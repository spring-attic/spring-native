#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_http user:password localhost:8080 "Hello from tomcat"

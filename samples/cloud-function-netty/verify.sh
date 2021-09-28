#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_command_output "curl -s localhost:8080/ -d world -H 'Content-Type: text/plain'" "hi world!"

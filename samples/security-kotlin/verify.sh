#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_command_output 'curl -I localhost:8080/' "HTTP/1.1 401"

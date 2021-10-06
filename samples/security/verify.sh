#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_log target/native/test-output.txt "Started SecuringWebApplication"' ERR
trap 'wait_http localhost:8080/home "Welcome"' ERR
wait_command_output 'curl -I localhost:8080/hello' "HTTP/1.1 302"

#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_log target/native/test-output.txt "Started SecurityWebfluxApplication"' ERR
trap 'wait_http localhost:8080/hello "Unauthorized"' ERR
trap 'wait_http user:password@localhost:8080/hello "Hello!"' ERR
trap 'wait_http user:password@localhost:8080/admin "Forbidden"' ERR
wait_http admin:password@localhost:8080/admin "Welcome to the administrator page!"

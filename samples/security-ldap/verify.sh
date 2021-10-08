#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_log target/native/test-output.txt "Started SecurityLdapApplication"' ERR
trap 'wait_http localhost:8080/ "Unauthorized"' ERR
trap 'wait_http user:password@localhost:8080/ "Hello, user!"' ERR
wait_http user:password@localhost:8080/friendly "Hello, Dianne Emu!"
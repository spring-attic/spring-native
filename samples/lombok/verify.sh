#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_log target/native/test-output.txt "Hello, Apache Logging world!"' ERR
wait_log target/native/test-output.txt "Hello, Slf4j world!"

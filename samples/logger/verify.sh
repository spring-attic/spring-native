#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_log target/native/test-output.txt "INFO com.example.logger.LoggerApplication - info"' ERR
trap 'wait_log target/native/test-output.txt "ERROR com.example.logger.LoggerApplication - ouch"' ERR
wait_log target/native/test-output.txt "java.lang.RuntimeException"

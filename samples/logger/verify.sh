#!/usr/bin/env bash
<<<<<<< HEAD
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_log target/native/test-output.txt "INFO com.example.logger.LoggerApplication - info" || RC=$?
wait_log target/native/test-output.txt "ERROR com.example.logger.LoggerApplication - ouch" || RC=$?
wait_log target/native/test-output.txt "java.lang.RuntimeException" || RC=$?

exit $RC
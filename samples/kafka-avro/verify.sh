#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_log target/native/test-output.txt "++++++Received Thing"
wait_log target/native/test-output.txt "++++++Received Thing2"
wait_log target/native/test-output.txt "++++++Received Thing3"

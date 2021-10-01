#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_log build/native/test-output.txt "commandlinerunner running!"

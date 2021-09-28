#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_log target/native/test-output.txt "commandlinerunner running!"' ERR
wait_log target/native/test-output.txt "ApplicationContextAware callback invoked"

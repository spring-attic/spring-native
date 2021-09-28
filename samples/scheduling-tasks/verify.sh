#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_log target/native/test-output.txt "The number is now 6"' ERR
wait_log target/native/test-output.txt "The other number is now 6"

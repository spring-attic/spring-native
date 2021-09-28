#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_log target/native/test-output.txt "EL: Received hello event: andy"' ERR
wait_log target/native/test-output.txt "EL: Received hello event: sebastien"

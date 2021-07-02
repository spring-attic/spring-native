#!/usr/bin/env bash
OUTPUT_FILE="target/native/test-output.txt"
source ${PWD%/*samples/*}/scripts/wait.sh

RC=0

wait_log "$OUTPUT_FILE" "java.lang.RuntimeException" || RC=$?
grep -q -E -e "[0-9]+ \[           main\] INFO  com.example.logger.LoggerApplication     - info" "$OUTPUT_FILE" || RC=$?
grep -q -E -e "[0-9]+ \[           main\] ERROR com.example.logger.LoggerApplication     - ouch" "$OUTPUT_FILE" || RC=$?

exit $RC

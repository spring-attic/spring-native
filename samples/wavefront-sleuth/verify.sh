#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_command_output 'curl -s localhost:8080/ -H "X-B3-TraceId: 1bf6ce8439eb15b8" -H "X-B3-SpanId: 1bf6ce8439eb15b8"' "Hello from tomcat [1bf6ce8439eb15b8]"

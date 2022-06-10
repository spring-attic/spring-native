#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_log target/native/test-output.txt "Started ZipkinApplication"
curl --fail -s localhost:8080/ -H "X-B3-TraceId: 1bf6ce8439eb15b8" -H "X-B3-SpanId: 1bf6ce8439eb15b8"
sleep 5 # sending of spans is done asynchronously - will wait 5 second to be sure
echo "Checking if the trace is in Zipkin"
curl --fail http://localhost:9411/api/v2/trace/1bf6ce8439eb15b8

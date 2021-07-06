#!/usr/bin/env bash
sleep 5
RESPONSE=`curl -s localhost:8080/ -H "X-B3-TraceId: 1bf6ce8439eb15b8" -H "X-B3-SpanId: 1bf6ce8439eb15b8"`
if [[ "$RESPONSE" == 'Hello from tomcat [1bf6ce8439eb15b8]' ]]; then
  exit 0
else
  exit 1
fi

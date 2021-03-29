#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/greeting`
if [[ "$RESPONSE" == *"Hello, World!"* && "$RESPONSE" == *"<title>"* ]]; then
  RESPONSE=`curl -s localhost:8080/greetings`
  if [[ "$RESPONSE" == *"<td>foo</td>"* && "$RESPONSE" == *"<td>bar</td>"* ]]; then
    exit 0
  else
    exit 1
  fi
else
  exit 1
fi

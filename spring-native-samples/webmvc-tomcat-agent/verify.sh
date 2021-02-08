#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8081/`
if [[ "$RESPONSE" == 'Hello from Spring MVC and Tomcat' ]]; then
  exit 0
else
  exit 1
fi

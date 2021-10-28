#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/`
if [[ "$RESPONSE" == 'Hello from Spring MVC and Tomcat' ]]; then
  RESPONSE=`curl -s localhost:8080/foo.html`
  if [[ "$RESPONSE" == 'Foo' ]]; then
    exit 0
  else
    exit 1
  fi
else
  exit 1
fi

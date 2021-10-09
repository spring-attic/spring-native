#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "Started MethodSecurityApplication"` ]]; then
  RESPONSE=`curl admin:password@localhost:8080/admin/private`
  if [[ "$RESPONSE" != 'bye' ]]; then
    echo $RESPONSE
    exit 2
  fi
  exit 0
else
  exit 1
fi


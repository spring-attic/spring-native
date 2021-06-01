#!/usr/bin/env bash
if [[ `cat target/native/test-output.txt | grep "Started MethodSecurityApplication"` ]]; then

RESPONSE=`curl localhost:8080/hello`
if [[ "$RESPONSE" != *'Unauthorized'* ]]; then
  echo $RESPONSE
  exit 1
fi

RESPONSE=`curl user:password@localhost:8080/hello`
if [[ "$RESPONSE" != 'Hello!' ]]; then
  echo $RESPONSE
  exit 2
fi

RESPONSE=`curl admin:password@localhost:8080/admin/hello`
if [[ "$RESPONSE" != 'Goodbye!' ]]; then
  echo $RESPONSE
  exit 3
fi

exit 0
else
  exit 4
fi

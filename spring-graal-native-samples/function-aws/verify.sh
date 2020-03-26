#!/usr/bin/env bash

JARDIR=target/native-image
java -cp $JARDIR/BOOT-INF/lib/*:$JARDIR/BOOT-INF/classes:$JARDIR:target/test-classes com.example.test.TestServer &
SPID=$!
sleep 5

curl -s localhost:8000/add -d world -H "Content-Type: text/plain"
echo
echo Waiting...
sleep 1
RESPONSE=`curl -s localhost:8000/take`
kill $SPID
echo Got response: $RESPONSE
if [[ "$RESPONSE" == 'hi world!' ]]; then
  exit 0
else
  exit 1
fi

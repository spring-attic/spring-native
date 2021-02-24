#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080`
if [[ $RESPONSE != *"Welcome"* ]]; then
 exit 1
fi

RESPONSE=`curl -s http://localhost:8080/owners/7`
if [[ $RESPONSE != *"Jeff Black"* ]]; then
 exit 2
fi

RESPONSE=`curl -s http://localhost:8080/vets.html`
if [[ $RESPONSE != *"James Carter"* ]]; then
 exit 3
fi

RESPONSE=`curl -Ls "http://localhost:8080/owners?lastName=Frank"`
if [[ $RESPONSE != *"George Franklin"* ]]; then
 exit 4
fi

exit 0


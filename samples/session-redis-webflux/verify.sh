#!/usr/bin/env bash
RESPONSE=`curl -u user:password localhost:8080`
if [[ "$RESPONSE" == *"This page is secured"* ]]; then
  exit 0
else
  exit 1
fi
#!/usr/bin/env bash
sleep 10
if [[  `curl localhost:8761/ | grep -E "DISCOVERY-CLIENT"` ]]; then
  exit 0
else
  exit 1
fi

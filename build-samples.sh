#!/usr/bin/env bash

for d in spring-graal-native-samples/*/
do
  if [[ -f "$d/build.sh" ]]; then
    (cd "$d" && ./build.sh)
  fi
done
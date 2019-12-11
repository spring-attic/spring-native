#!/usr/bin/env bash

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

EXECUTABLE=./target/vanilla-orm2

if [[ ! -f $EXECUTABLE ]]
then
  printf "${RED}FAILURE${NC}: the executable has not been generated\n"
  exit 1
fi

echo "Testing $EXECUTABLE output"
if [[ ! `$EXECUTABLE 2>&1 | grep -E "Application run failed|No suitable logging system located"` ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
  exit 0
else
  printf "${RED}FAILURE${NC}: the output of the application does not contain the expected output\n"
  exit 1
fi

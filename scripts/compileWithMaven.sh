#!/usr/bin/env bash

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"

rm -rf target
mkdir -p target/native

echo "Packaging ${PWD##*/} with Maven"
# Only run Petclinic tests to speedup the full build while still testing a complex testing scenario
if [[ ${PWD##*/} == petclinic* || ${PWD##*/} == *agent* ]]
then
  echo "Performing native testing for ${PWD##*/}"
  mvn -ntp -Pnative package $* &> target/native/output.txt
else
  mvn -ntp -DskipTests -Pnative package $* &> target/native/output.txt
fi


if [[ -f target/${PWD##*/} ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
else
  cat target/native/output.txt
  printf "${RED}FAILURE${NC}: an error occurred when compiling the native image.\n"
  exit 1
fi

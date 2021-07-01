#!/usr/bin/env bash

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"

rm -rf build
mkdir -p build/native

echo "Packaging ${PWD##*/} with Gradle"
# Only run security-kotlin tests to speedup the full build while still testing a Gradle project
if [[ ${PWD##*/} == "security-kotlin" ]]
then
  echo "Performing native testing for ${PWD##*/}"
  ./gradlew nativeTest nativeBuild &> build/native/output.txt
else
  ./gradlew nativeBuild &> build/native/output.txt
fi

if [[ -f build/native/${PWD##*/} ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
else
  cat build/native/output.txt
  printf "${RED}FAILURE${NC}: an error occurred when compiling the native-image.\n"
  exit 1
fi

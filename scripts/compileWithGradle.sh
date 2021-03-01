#!/usr/bin/env bash

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"

rm -rf build
mkdir -p build/native-image

echo "Packaging ${PWD##*/} with Gradle"
./gradlew assemble &> build/native-image/output.txt

if [[ -f build/libs/${PWD##*/}-0.0.1-SNAPSHOT.jar ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
else
  cat build/native-image/output.txt
  printf "${RED}FAILURE${NC}: an error occurred when compiling the native-image.\n"
  exit 1
fi

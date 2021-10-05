#!/usr/bin/env bash

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

AOT_ONLY=false

while test $# -gt 0; do
  case "$1" in
    -a)
      export AOT_ONLY=true
      shift
      ;;
    --aot-only)
      export AOT_ONLY=true
      shift
      ;;
    *)
      break
      ;;
  esac
done

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"

rm -rf build
mkdir -p build/native

if [ "$AOT_ONLY" = false ] ; then
  echo "Packaging ${PWD##*/} with Gradle (native)"
  ./gradlew nativeCompile &> build/native/output.txt

  if [[ -f build/native/nativeCompile/${PWD##*/} ]]; then
    printf "${GREEN}SUCCESS${NC}\n"
  else
    cat build/native/output.txt
    printf "${RED}FAILURE${NC}: an error occurred when compiling the native-image.\n"
    exit 1
  fi
else
  echo "Packaging ${PWD##*/} with Gradle (AOT only)'"
  ./gradlew bootJar &> build/native/output.txt
fi

#!/usr/bin/env bash

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

AOT_ONLY=false
NATIVE_TESTS=false
NICENESS=0

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
    -t)
      export NATIVE_TESTS=true
      shift
      ;;
    --native-tests)
      export NATIVE_TESTS=true
      shift
      ;;
    -l)
      export NICENESS=19
      shift
      ;;
    --low-priority)
      export NICENESS=19
      shift
      ;;
    -q)
      printf "${RED}Quick mode with gradle builds is not implemented at the moment${NC}\n"
      shift
      ;;
    --quick)
      printf "${RED}Quick mode with gradle builds is not implemented at the moment${NC}\n"
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
  if [ "$NATIVE_TESTS" = false ] ; then
    nice -n $NICENESS ./gradlew nativeCompile &> build/native/output.txt
  else
    nice -n $NICENESS ./gradlew nativeTest nativeCompile &> build/native/output.txt
  fi

  if [[ -f build/native/nativeCompile/${PWD##*/} ]]; then
    printf "${GREEN}SUCCESS${NC}\n"
  else
    cat build/native/output.txt
    printf "${RED}FAILURE${NC}: an error occurred when compiling the native-image.\n"
    exit 1
  fi
else
  echo "Packaging ${PWD##*/} with Gradle (AOT only)'"
  if nice -n $NICENESS ./gradlew build &> build/native/output.txt; then
    printf "${GREEN}SUCCESS${NC}\n"
  else
    cat build/native/output.txt
    printf "${RED}FAILURE${NC}: an error occurred when building the JAR in AOT mode.\n"
    exit 1
  fi
fi

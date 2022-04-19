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
    *)
      break
      ;;
  esac
done

if [ -z "$DB" ]; then
  printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"
else
  printf "=== ${BLUE}Building %s sample with database ${DB}${NC} ===\n" "${PWD##*/}"
fi

rm -rf target
mkdir -p target/native

if [ "$AOT_ONLY" = false ] ; then
  echo "Packaging ${PWD##*/} with Maven (native)"
  if [[ ${PWD##*/} == *-agent ]] ; then
    nice -n $NICENESS mvn test &> target/native/output.txt
  fi
  if [ "$NATIVE_TESTS" = false ] ; then
    nice -n $NICENESS mvn -ntp -DskipTests -Pnative package $* &> target/native/output.txt
  else
    nice -n $NICENESS mvn -ntp -Pnative package $* &> target/native/output.txt
  fi

  if [[ -f target/${PWD##*/} ]]; then
    printf "${GREEN}SUCCESS${NC}\n"
  else
    cat target/native/output.txt
    printf "${RED}FAILURE${NC}: an error occurred when compiling the native image.\n"
    exit 1
  fi
else
  echo "Packaging ${PWD##*/} with Maven (AOT only)"
  if nice -n $NICENESS mvn -ntp package $* &> target/native/output.txt; then
    printf "${GREEN}SUCCESS${NC}\n"
  else
    cat target/native/output.txt
    printf "${RED}FAILURE${NC}: an error occurred when building the JAR in AOT mode.\n"
    exit 1
  fi
fi

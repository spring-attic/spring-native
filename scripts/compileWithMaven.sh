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

rm -rf target
mkdir -p target/native

if [ "$AOT_ONLY" = false ] ; then
  echo "Packaging ${PWD##*/} with Maven (native)"
  mvn -ntp -DskipTests -Pnative package $* &> target/native/output.txt

  if [[ -f target/${PWD##*/} ]]; then
    printf "${GREEN}SUCCESS${NC}\n"
  else
    cat target/native/output.txt
    printf "${RED}FAILURE${NC}: an error occurred when compiling the native image.\n"
    exit 1
  fi
else
  echo "Packaging ${PWD##*/} with Maven (AOT only)"
  if mvn -ntp -DskipTests package $* &> target/native/output.txt; then
    printf "${GREEN}SUCCESS${NC}\n"
  else
    cat target/native/output.txt
    printf "${RED}FAILURE${NC}: an error occurred when building the JAR in AOT mode.\n"
    exit 1
  fi
fi

#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"
rm -rf target/
mkdir -p target/native-image 2>/dev/null
mvn -ntp -Pnative package > target/native-image/output.txt
${PWD%/*samples/*}/scripts/test.sh

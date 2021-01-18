#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"
mvn -Pbti-native clean package 2>&1 > output.txt
mkdir -p target/native-image
mv output.txt target/native-image/.
${PWD%/*samples/*}/scripts/test.sh

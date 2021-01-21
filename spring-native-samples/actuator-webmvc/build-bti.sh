#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"
mvn -Pbti-native clean package 2>&1 > output.txt
#-Dspring.native.build-time-properties-checks=default-include-all -Dspring.native.build-time-properties-match-if-missing=false -Dspring.native.factories.no-actuator-metrics=true -Dspring.native.remove-yaml-support=true 
mkdir -p target/native-image
mv output.txt target/native-image/.
${PWD%/*samples/*}/scripts/test.sh
mv target/native-image/summary.csv target/native-image/summary-bti.csv 

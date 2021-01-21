#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'
SAMPLE=$1

printf "=== ${BLUE}Building $SAMPLE sample (bti-native profile)${NC} ===\n"
cd spring-native-samples/$SAMPLE
ARGS=`grep "<build.args>" pom.xml | sed 's/<[^>]*>//g' | sed 's/^[ 	]*//'`
if [ ! -z "$ARGS" ]; then
	echo "Arguments to build process: $ARGS"
fi
mvn $ARGS -Pbti-native clean package 2>&1 > output.txt
mkdir -p target/native-image
mv output.txt target/native-image/.
${PWD%/*samples/*}/scripts/test.sh

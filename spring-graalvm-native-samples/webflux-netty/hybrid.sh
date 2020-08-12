#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

EXEC=`basename $PWD`-hybrid

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"
mvn -Phybrid clean package > output.txt 2>&1 && cp target/$EXEC . && ${PWD%/*samples/*}/scripts/test.sh $EXEC .
rm $EXEC
mv output.txt target/.

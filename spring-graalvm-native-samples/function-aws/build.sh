#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"

./compile.sh || exit 1

JARDIR=target/native-image
java -cp $JARDIR/BOOT-INF/lib/*:$JARDIR/BOOT-INF/classes:$JARDIR:target/test-classes com.example.test.TestServer &
SPID=$!
sleep 5

${PWD%/*samples/*}/scripts/test.sh --spring.cloud.function.web.export.source.url=http://localhost:8000/home --spring.cloud.function.web.export.sink.url=http://localhost:8000/echo

kill $SPID

#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"

./compile.sh && ${PWD%/*samples/*}/scripts/test.sh --spring.cloud.function.web.export.source.url=http://localhost:8000/home --spring.cloud.function.web.export.sink.url=http://localhost:8000/echo

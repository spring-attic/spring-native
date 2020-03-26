#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"

export SPRING_CLOUD_FUNCTION_WEB_EXPORT_SOURCE_URL=http://localhost:8000/home
export SPRING_CLOUD_FUNCTION_WEB_EXPORT_SINK_URL=http://localhost:8000/echo

./compile.sh && ${PWD%/*samples/*}/scripts/test.sh

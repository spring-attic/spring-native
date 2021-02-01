#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"
docker-compose up -d neo4j && ./compile.sh && ${PWD%/*samples/*}/scripts/test.sh && docker-compose down

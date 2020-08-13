#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

echo "docker run -p 8080:8080 docker.io/library/webflux-netty:0.0.1-SNAPSHOT" > target/webflux-netty-hybrid.sh
chmod +x target/webflux-netty-hybrid.sh
printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"
mvn -Phybrid spring-boot:build-image > output.txt 2>&1 && ${PWD%/*samples/*}/scripts/test.sh target/webflux-netty-hybrid.sh .
mv output.txt target/.
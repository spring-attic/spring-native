#!/usr/bin/env bash

docker-compose up -d mysql-server && ${PWD%/*samples/*}/scripts/compileWithMaven.sh && ${PWD%/*samples/*}/scripts/test.sh && docker-compose down
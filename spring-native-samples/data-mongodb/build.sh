#!/usr/bin/env bash

docker-compose up -d mongo && ${PWD%/*samples/*}/scripts/compileWithMaven.sh && ${PWD%/*samples/*}/scripts/test.sh && docker-compose down
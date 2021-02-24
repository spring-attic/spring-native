#!/usr/bin/env bash
set -e

docker-compose up -d lambda
${PWD%/*samples/*}/scripts/compileWithMaven.sh
AWS_LAMBDA_RUNTIME_API=localhost:9001 _HANDLER=foobar ${PWD%/*samples/*}/scripts/test.sh
docker-compose down

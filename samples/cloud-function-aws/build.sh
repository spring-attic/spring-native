#!/usr/bin/env bash

RC=0

docker-compose up -d lambda
${PWD%/*samples/*}/scripts/compileWithMaven.sh
AWS_LAMBDA_RUNTIME_API=localhost:9001 _HANDLER=foobar ${PWD%/*samples/*}/scripts/test.sh || RC=$?
docker-compose down

exit $RC
#!/usr/bin/env bash

RC=0

docker-compose up -d
${PWD%/*samples/*}/scripts/compileWithMaven.sh $* &&  ${PWD%/*samples/*}/scripts/test.sh $*
docker-compose down

exit $RC
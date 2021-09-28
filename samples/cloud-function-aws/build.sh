#!/usr/bin/env bash

RC=0

docker-compose up -d
${PWD%/*samples/*}/scripts/compileWithMaven.sh $*
docker-compose down

exit $RC
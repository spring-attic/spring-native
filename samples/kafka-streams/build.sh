#!/usr/bin/env bash

RC=0

docker-compose up -d
${PWD%/*samples/*}/scripts/compileWithMaven.sh $* && sleep 10 && ${PWD%/*samples/*}/scripts/test.sh $* || RC=$?
docker-compose down

exit $RC

#!/usr/bin/env bash

RC=0

docker-compose up -d
sleep 10s # We need to wait until the docker container has been started and accepts connections
${PWD%/*samples/*}/scripts/compileWithMaven.sh $* &&  ${PWD%/*samples/*}/scripts/test.sh $* || RC=$?
docker-compose down

exit $RC

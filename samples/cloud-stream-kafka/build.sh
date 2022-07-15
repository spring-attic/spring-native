#!/usr/bin/env bash

docker-compose up -d

${PWD%/*samples/*}/scripts/install3rdPartyHints.sh && ${PWD%/*samples/*}/scripts/compileWithMaven.sh $* &&  ${PWD%/*samples/*}/scripts/test.sh $*

docker-compose down

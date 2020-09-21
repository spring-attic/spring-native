#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"

./compile.sh || exit 1

JARDIR=target/native-image
docker run --cidfile=.cid -p 9001:9001 -e DOCKER_LAMBDA_STAY_OPEN=1 -v `pwd`/src/test/resources:/var/task lambci/lambda:provided
SPID=$(cat .cid)
sleep 5

AWS_LAMBDA_RUNTIME_API=localhost:9001 _HANDLER=foobar ${PWD%/*samples/*}/scripts/test.sh 

docker kill $SPID

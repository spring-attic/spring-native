#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

printf "=== ${BLUE}Building %s sample${NC} ===\n" "${PWD##*/}"

./compile.sh || exit 1

JARDIR=target/native-image
rm -f target/.cid
echo -n "" > target/docker.log
docker run --cidfile=target/.cid -p 9001:9001 -e DOCKER_LAMBDA_STAY_OPEN=1 -v `pwd`/src/test/resources:/var/task lambci/lambda:provided >target/docker.log 2>&1  &
tail -f target/docker.log | sed '/Lambda API listening on port 9001/ q'

AWS_LAMBDA_RUNTIME_API=localhost:9001 _HANDLER=foobar ${PWD%/*samples/*}/scripts/test.sh 

docker kill $(cat target/.cid)

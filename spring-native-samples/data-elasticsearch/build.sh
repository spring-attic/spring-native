#!/usr/bin/env bash

docker-compose up -d elasticsearch
${PWD%/*samples/*}/scripts/compileWithMaven.sh && ${PWD%/*samples/*}/scripts/test.sh
docker-compose down
#!/usr/bin/env bash

native-image --version
/bin/start-docker.sh
if [[ -n $DOCKER_HUB_USERNAME ]]; then
	echo "$DOCKER_HUB_PASSWORD" | docker login -u $DOCKER_HUB_USERNAME --password-stdin
fi
cd spring-native
./build.sh
./build-key-samples.sh

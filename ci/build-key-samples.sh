#!/usr/bin/env bash

native-image --version
/bin/start-docker.sh
if [[ -n $DOCKER_HUB_USERNAME ]]; then
	docker login -u $DOCKER_HUB_USERNAME -p $DOCKER_HUB_PASSWORD
fi
cd spring-native
./build.sh
./build-key-samples.sh

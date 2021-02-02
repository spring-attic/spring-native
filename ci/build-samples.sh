#!/usr/bin/env sh

RC=0
native-image --version
/bin/start-docker.sh
if [[ -n $DOCKER_HUB_USERNAME ]]; then
	docker login -u $DOCKER_HUB_USERNAME -p $DOCKER_HUB_PASSWORD
fi
cd spring-native
if ! (./build.sh); then
    RC=1
fi
if ! (./build-samples.sh); then
    RC=1
fi
exit $RC
#!/usr/bin/env bash

RC=0

echo "GraalVM build information"
echo ""
cat $JAVA_HOME/release
echo ""

/bin/start-docker.sh
if [[ -n $DOCKER_HUB_USERNAME ]]; then
	echo "$DOCKER_HUB_PASSWORD" | docker login -u $DOCKER_HUB_USERNAME --password-stdin
fi
cd git-repo
if ! (./build.sh); then
    RC=1
fi
if ! (./build-key-samples.sh); then
    RC=1
fi
exit $RC

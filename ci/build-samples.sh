#!/usr/bin/env sh

RC=0
native-image --version
/bin/start-docker.sh
cd spring-native
if ! (./build.sh); then
    RC=1
fi
if ! (./build-samples.sh); then
    RC=1
fi
exit $RC
#!/usr/bin/env sh

native-image --version
/bin/start-docker.sh
cd spring-native
./build.sh
./build-key-samples.sh

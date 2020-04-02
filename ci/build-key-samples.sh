#!/usr/bin/env sh

cd spring-graal-native
. ci/setup.sh
./build.sh
./build-key-samples.sh

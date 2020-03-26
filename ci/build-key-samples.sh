#!/usr/bin/env sh

cd spring-graal-native
. ci/setup.sh
./build-feature.sh
./build-key-samples.sh

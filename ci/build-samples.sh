#!/usr/bin/env sh

./setup.sh

cd spring-graal-native
./build-feature.sh
./build-samples.sh

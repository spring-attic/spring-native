#!/usr/bin/env sh

./setup.sh

cd spring-graal-native
./build-feature.sh
./build-key-samples.sh

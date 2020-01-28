#!/usr/bin/env sh

set -e

cd spring-graal-native
./build-feature.sh
./build-key-samples.sh

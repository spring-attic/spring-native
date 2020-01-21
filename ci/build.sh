#!/usr/bin/env sh

set -e

gu install native-image
cd spring-graal-native
./build-feature.sh
./build-key-samples.sh

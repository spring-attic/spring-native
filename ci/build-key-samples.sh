#!/usr/bin/env sh

native-image --version
cd spring-native
./build.sh
./build-key-samples.sh

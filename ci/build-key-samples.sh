#!/usr/bin/env sh

native-image --version
cd spring-graalvm-native
./build.sh
./build-key-samples.sh

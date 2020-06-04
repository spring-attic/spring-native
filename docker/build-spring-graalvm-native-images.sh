#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
echo $DIR


docker build \
  --build-arg BASE_IMAGE=springci/graalvm-ce:20.1-dev-java8 \
  -t springci/spring-graalvm-native:20.1-dev-java8 - < $DIR/Dockerfile.spring-graalvm-native

docker build \
  --build-arg BASE_IMAGE=springci/graalvm-ce:20.1-dev-java11 \
  -t springci/spring-graalvm-native:20.1-dev-java11 - < $DIR/Dockerfile.spring-graalvm-native

docker build \
  --build-arg BASE_IMAGE=springci/graalvm-ce:master-java8 \
  -t springci/spring-graalvm-native:master-java8 - < $DIR/Dockerfile.spring-graalvm-native

docker build \
  --build-arg BASE_IMAGE=springci/graalvm-ce:master-java11 \
  -t springci/spring-graalvm-native:master-java11 - < $DIR/Dockerfile.spring-graalvm-native

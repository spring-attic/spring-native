#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
DEV_IMAGE=$(test $OSTYPE = "darwin" && echo "Dockerfile.spring-graalvm-native-dev-mac" || echo "Dockerfile.spring-graalvm-native-dev")
echo "Using $DEV_IMAGE since OSTYPE is $OSTYPE"

# Update remote images
docker pull springci/spring-graalvm-native:20.1-dev-java8
docker pull springci/spring-graalvm-native:20.1-dev-java11
docker pull springci/spring-graalvm-native:master-java8
docker pull springci/spring-graalvm-native:master-java11

docker build \
  --build-arg BASE_IMAGE=springci/spring-graalvm-native:20.1-dev-java8 \
  --build-arg USER=$USER \
  --build-arg USER_ID=$(id -u ${USER}) \
  --build-arg USER_GID=$(id -g ${USER}) \
  -t spring-graalvm-native-dev:20.1-dev-java8 - < $DIR/$DEV_IMAGE

docker build \
  --build-arg BASE_IMAGE=springci/spring-graalvm-native:20.1-dev-java11 \
  --build-arg USER=$USER \
  --build-arg USER_ID=$(id -u ${USER}) \
  --build-arg USER_GID=$(id -g ${USER}) \
  -t spring-graalvm-native-dev:20.1-dev-java11 - < $DIR/$DEV_IMAGE

docker build \
  --build-arg BASE_IMAGE=springci/spring-graalvm-native:master-java8 \
  --build-arg USER=$USER \
  --build-arg USER_ID=$(id -u ${USER}) \
  --build-arg USER_GID=$(id -g ${USER}) \
  -t spring-graalvm-native-dev:master-java8 - < $DIR/$DEV_IMAGE

docker build \
  --build-arg BASE_IMAGE=springci/spring-graalvm-native:master-java11 \
  --build-arg USER=$USER \
  --build-arg USER_ID=$(id -u ${USER}) \
  --build-arg USER_GID=$(id -g ${USER}) \
  -t spring-graalvm-native-dev:master-java11 - < $DIR/$DEV_IMAGE
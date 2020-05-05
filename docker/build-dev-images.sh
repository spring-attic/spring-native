#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

docker build \
  --no-cache \
  --build-arg BASE_IMAGE=springci/spring-graal-native:20.1-dev-java8 \
  --build-arg USER=$USER \
  --build-arg USER_ID=$(id -u ${USER}) \
  --build-arg USER_GID=$(id -g ${USER}) \
  -t spring-graal-native-dev:20.1-dev-java8 - < $DIR/Dockerfile.spring-graal-native-dev

docker build \
  --no-cache \
  --build-arg BASE_IMAGE=springci/spring-graal-native:20.1-dev-java11 \
  --build-arg USER=$USER \
  --build-arg USER_ID=$(id -u ${USER}) \
  --build-arg USER_GID=$(id -g ${USER}) \
  -t spring-graal-native-dev:20.1-dev-java11 - < $DIR/Dockerfile.spring-graal-native-dev

docker build \
  --no-cache \
  --build-arg BASE_IMAGE=springci/spring-graal-native:master-java8 \
  --build-arg USER=$USER \
  --build-arg USER_ID=$(id -u ${USER}) \
  --build-arg USER_GID=$(id -g ${USER}) \
  -t spring-graal-native-dev:master-java8 - < $DIR/Dockerfile.spring-graal-native-dev

docker build \
  --no-cache \
  --build-arg BASE_IMAGE=springci/spring-graal-native:master-java11 \
  --build-arg USER=$USER \
  --build-arg USER_ID=$(id -u ${USER}) \
  --build-arg USER_GID=$(id -g ${USER}) \
  -t spring-graal-native-dev:master-java11 - < $DIR/Dockerfile.spring-graal-native-dev
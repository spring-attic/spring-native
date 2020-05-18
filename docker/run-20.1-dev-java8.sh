#!/usr/bin/env bash

CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." >/dev/null 2>&1 && pwd )"
CONTAINER_HOME=/home/$USER
WORK_DIR=$CONTAINER_HOME/spring-graalvm-native
docker run --hostname docker -v $CURRENT_DIR:$WORK_DIR -v $HOME/.m2:$CONTAINER_HOME/.m2 -it -w $WORK_DIR -u $(id -u ${USER}):$(id -g ${USER}) spring-graalvm-native-dev:20.1-dev-java8
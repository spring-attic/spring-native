#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." >/dev/null 2>&1 && pwd )"
WORKDIR=/home/$USER/spring-graal-native
docker run --hostname docker -v $DIR:$WORKDIR -it -w $WORKDIR -u $(id -u ${USER}):$(id -g ${USER}) spring-graal-native-dev:20.1-dev-java8
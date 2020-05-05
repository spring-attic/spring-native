#!/usr/bin/env bash

docker push springci/graalvm-ce:20.1-dev-java8
docker push springci/graalvm-ce:20.1-dev-java11

docker push springci/graalvm-ce:master-java8
docker push springci/graalvm-ce:master-java11

docker push springci/spring-graal-native:20.1-dev-java8
docker push springci/spring-graal-native:20.1-dev-java11

docker push springci/spring-graal-native:master-java8
docker push springci/spring-graal-native:master-java11

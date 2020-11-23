#!/usr/bin/env bash

docker push springci/spring-graalvm-native:20.3-dev-java8
docker push springci/spring-graalvm-native:20.3-dev-java11
docker push springci/spring-graalvm-native:master-java8
docker push springci/spring-graalvm-native:master-java11

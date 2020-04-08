#!/usr/bin/env sh

set -e

cd spring-graal-native
./mvnw deploy -P artifactory,docs -Dartifactory.username=$ARTIFACTORY_USERNAME -Dartifactory.password=$ARTIFACTORY_PASSWORD

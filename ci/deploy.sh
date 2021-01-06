#!/usr/bin/env sh

set -e

cd spring-native
./mvnw -ntp deploy -P artifactory,docs -Dartifactory.username=$ARTIFACTORY_USERNAME -Dartifactory.password=$ARTIFACTORY_PASSWORD

#!/bin/bash

source $(dirname $0)/common.sh
CONFIG_DIR=git-repo/ci/config

version=$( cat artifactory-repo/build-info.json | jq -r '.buildInfo.modules[0].id' | sed 's/.*:.*:\(.*\)/\1/' )
export BUILD_INFO_LOCATION=$(pwd)/artifactory-repo/build-info.json

java -jar /opt/concourse-release-scripts.jar \
  --spring.config.location=${CONFIG_DIR}/release-scripts.yml \
  promote $RELEASE_TYPE $BUILD_INFO_LOCATION || { exit 1; }

echo "Promotion complete"
echo $version > version/version

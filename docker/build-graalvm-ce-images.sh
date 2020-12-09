#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
echo $DIR


docker build \
  --build-arg JVMCI_URL=https://github.com/graalvm/graal-jvmci-8/releases/download/jvmci-20.3-b06/openjdk-8u272+10-jvmci-20.3-b06-linux-amd64.tar.gz \
  --build-arg GRAALVM_BRANCH=release/graal-vm/20.3 \
  -t springci/graalvm-ce:20.3-dev-java8 - < $DIR/Dockerfile.graalvm-ce

docker build \
  --build-arg JVMCI_URL=https://github.com/graalvm/labs-openjdk-11/releases/download/jvmci-20.3-b06/labsjdk-ce-11.0.9+10-jvmci-20.3-b06-linux-amd64.tar.gz \
  --build-arg GRAALVM_BRANCH=release/graal-vm/20.3 \
  -t springci/graalvm-ce:20.3-dev-java11 - < $DIR/Dockerfile.graalvm-ce

docker build \
  --build-arg JVMCI_URL=https://github.com/graalvm/graal-jvmci-8/releases/download/jvmci-21.0-b03/openjdk-8u272+10-jvmci-21.0-b03-linux-amd64.tar.gz \
  --build-arg GRAALVM_BRANCH=master \
  -t springci/graalvm-ce:master-java8 - < $DIR/Dockerfile.graalvm-ce

docker build \
  --build-arg JVMCI_URL=https://github.com/graalvm/labs-openjdk-11/releases/download/jvmci-21.0-b03/labsjdk-ce-11.0.9+10-jvmci-21.0-b03-linux-amd64.tar.gz \
  --build-arg GRAALVM_BRANCH=master \
  -t springci/graalvm-ce:master-java11 - < $DIR/Dockerfile.graalvm-ce

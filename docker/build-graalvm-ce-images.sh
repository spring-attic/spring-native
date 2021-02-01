#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
echo $DIR


docker build \
  --build-arg JVMCI_URL=https://github.com/graalvm/graal-jvmci-8/releases/download/jvmci-21.0-b06/openjdk-8u282+07-jvmci-21.0-b06-linux-amd64.tar.gz \
  --build-arg GRAALVM_BRANCH=release/graal-vm/21.0 \
  -t springci/graalvm-ce:21.0-dev-java8 -f $DIR/Dockerfile.graalvm-ce $DIR

docker build \
  --build-arg JVMCI_URL=https://github.com/graalvm/labs-openjdk-11/releases/download/jvmci-21.0-b06/labsjdk-ce-11.0.10+8-jvmci-21.0-b06-linux-amd64.tar.gz \
  --build-arg GRAALVM_BRANCH=release/graal-vm/21.0 \
  -t springci/graalvm-ce:21.0-dev-java11 -f $DIR/Dockerfile.graalvm-ce $DIR

docker build \
  --build-arg JVMCI_URL=https://github.com/graalvm/graal-jvmci-8/releases/download/jvmci-21.0-b06/openjdk-8u282+07-jvmci-21.0-b06-linux-amd64.tar.gz \
  --build-arg GRAALVM_BRANCH=master \
  -t springci/graalvm-ce:master-java8 -f $DIR/Dockerfile.graalvm-ce $DIR

docker build \
  --build-arg JVMCI_URL=https://github.com/graalvm/labs-openjdk-11/releases/download/jvmci-21.0-b06/labsjdk-ce-11.0.10+8-jvmci-21.0-b06-linux-amd64.tar.gz \
  --build-arg GRAALVM_BRANCH=master \
  -t springci/graalvm-ce:master-java11 -f $DIR/Dockerfile.graalvm-ce $DIR

FROM springci/ci-image:main

ARG LABSJDK_IDENTIFIER
ARG GRAALVM_BRANCH

ENV PATH="/opt/mx:$PATH"

RUN apt-get -y update \
 && apt-get -y install git jq curl build-essential python3.8 python3-pip unzip zlib1g-dev \
 && rm -rf /var/lib/apt/lists/*

RUN pip3 install ninja \
 && pip3 install ninja_syntax

RUN cd /opt \
 && git clone --single-branch --branch master https://github.com/graalvm/mx.git \
 && git clone --single-branch --branch $GRAALVM_BRANCH https://github.com/oracle/graal.git

RUN mx --quiet fetch-jdk --jdk-id $LABSJDK_IDENTIFIER --configuration /opt/graal/common.json --to /opt \
 || mx --quiet fetch-jdk --jdk-id $LABSJDK_IDENTIFIER --to /opt

RUN mv /opt/labsjdk-ce-* /opt/openjdk-jvmci

ENV JAVA_HOME=/opt/openjdk-jvmci

RUN cd /opt/graal/vm \
 && mx --disable-polyglot --disable-libpolyglot --dynamicimports /substratevm build

FROM ubuntu:focal

ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"

RUN apt-get -y update \
 && apt-get -y install build-essential zlib1g-dev \
 && rm -rf /var/lib/apt/lists/*

RUN mkdir /opt/java

COPY --from=0 /opt/graal/vm/latest_graalvm_home/. /opt/java/

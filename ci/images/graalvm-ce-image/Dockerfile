FROM springci/ci-image:main

ARG LABSJDK_IDENTIFIER
ARG GRAALVM_BRANCH

ENV PATH="/opt/mx:$PATH"

ENV MX_PYTHON="/usr/bin/python3.8"

RUN apt-get -y update \
 && apt-get -y install git jq curl build-essential python3.8 python3.8-distutils unzip zlib1g-dev \
 && rm -rf /var/lib/apt/lists/*

RUN curl https://bootstrap.pypa.io/get-pip.py --output /tmp/get-pip.py \
 && python3.8 /tmp/get-pip.py \
 && pip install ninja \
 && pip install ninja_syntax

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

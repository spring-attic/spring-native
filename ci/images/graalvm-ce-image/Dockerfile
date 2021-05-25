FROM ubuntu:focal

ARG JVMCI_URL
ARG GRAALVM_BRANCH

ENV JAVA_HOME=/opt/openjdk-jvmci
ENV PATH="/opt/mx:$JAVA_HOME/bin:$PATH"

RUN apt-get -y update \
 && apt-get -y install git jq curl build-essential python unzip zlib1g-dev \
 && rm -rf /var/lib/apt/lists/*

RUN cd /opt \
 && git clone --single-branch --branch master https://github.com/graalvm/mx.git \
 && curl -L $JVMCI_URL | tar -xz \
 && mv *jdk* openjdk-jvmci \
 && git clone --single-branch --branch $GRAALVM_BRANCH https://github.com/oracle/graal.git \
 && cd graal/vm \
 && mx --disable-polyglot --disable-libpolyglot --dynamicimports /substratevm build

FROM ubuntu:focal

ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"

RUN apt-get -y update \
 && apt-get -y install build-essential zlib1g-dev \
 && rm -rf /var/lib/apt/lists/*

RUN mkdir /opt/java

COPY --from=0 /opt/graal/vm/latest_graalvm_home/. /opt/java/

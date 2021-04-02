ARG BASE_IMAGE
FROM $BASE_IMAGE

ARG MAVEN_VERSION=3.6.3
ARG DOCKER_GID=130

ENV PATH="/opt:/opt/apache-maven-$MAVEN_VERSION/bin:$PATH" \
    DEBIAN_FRONTEND=noninteractive

RUN apt-get -y update \
 && apt-get -y install git jq curl python unzip bc bsdmainutils build-essential gdb net-tools iptables iproute2 sudo \
 && cd /opt \
 && curl -LO https://github.com/making/rsc/releases/download/0.7.1/rsc-x86_64-pc-linux \
 && mv rsc-x86_64-pc-linux rsc \
 && chmod +x rsc \
 && curl -LO https://repo1.maven.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip \
 && unzip apache-maven-${MAVEN_VERSION}-bin.zip \
 && rm apache-maven-${MAVEN_VERSION}-bin.zip \
 && curl -L https://github.com/fullstorydev/grpcurl/releases/download/v1.5.0/grpcurl_1.5.0_linux_x86_64.tar.gz | tar -xz \
 && rm LICENSE \
 && curl -sL https://deb.nodesource.com/setup_12.x | bash - \
 && apt-get -y install nodejs \
 && curl -LO https://github.com/tecfu/tty-table/archive/master.zip \
 && unzip master.zip \
 && rm master.zip \
 && cd tty-table-master \
 && npm i -g \
 && npm cache clean --force \
 && rm -rf /var/cache/apt/archives \
 && rm -rf /var/lib/apt/lists/* \
 && cd / \
 && curl -L https://download.docker.com/linux/static/stable/x86_64/docker-19.03.14.tgz | tar zx \
 && mv /docker/* /bin/ \
 && chmod +x /bin/docker* \
 && export ENTRYKIT_VERSION=0.4.0 \
 && curl -L https://github.com/progrium/entrykit/releases/download/v${ENTRYKIT_VERSION}/entrykit_${ENTRYKIT_VERSION}_Linux_x86_64.tgz | tar zx \
 && chmod +x entrykit \
 && mv entrykit /bin/entrykit \
 && entrykit --symlink \
 && groupadd -g $DOCKER_GID docker \
 && echo '%sudo ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers \
 && curl -L "https://github.com/docker/compose/releases/download/1.28.2/docker-compose-$(uname -s)-$(uname -m)" -o /bin/docker-compose \
 && chmod +x /bin/docker-compose

ADD docker-lib.sh /bin/docker-lib.sh
ADD start-docker.sh /bin/start-docker.sh

CMD native-image --version && sudo /bin/start-docker.sh && /bin/bash

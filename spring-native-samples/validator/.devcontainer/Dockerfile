FROM springci/spring-native:21.0-dev-java11

ARG USER
ARG USER_ID
ARG USER_GID

RUN (groupadd --gid "${USER_GID}" "${USER}" || echo "No groupadd needed") && \
    useradd \
      --uid ${USER_ID} \
      --gid ${USER_GID} \
      -G sudo,docker \
      --create-home \
      --shell /bin/bash ${USER} && \
      usermod -aG docker $USER

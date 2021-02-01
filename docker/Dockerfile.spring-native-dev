ARG BASE_IMAGE
FROM $BASE_IMAGE

ARG USER
ARG USER_ID
ARG USER_GID

RUN (groupadd --gid "${USER_GID}" "${USER}" || echo "No groupadd needed") && \
    useradd \
      --uid ${USER_ID} \
      --gid ${USER_GID} \
      -G sudo,docker \
      --create-home \
      --shell /bin/bash \
      ${USER}

USER ${USER}
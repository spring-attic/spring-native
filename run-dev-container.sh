#!/usr/bin/env bash

JAVA_VERSION=8
GRAALVM_VERSION=20.1-dev
PULL=false
REBUILD=false
HOST_WORK_DIR="$( pwd )"

while test $# -gt 0; do
  case "$1" in
    -h|--help)
      echo "run-dev-container.sh - run Spring GraalVM native dev container"
      echo " "
      echo "run-dev-container.sh [options]"
      echo " "
      echo "options:"
      echo "-h, --help                show brief help"
      echo "-j, --java=VERSION        specify Java version to use, can be 8 or 11, 8 by default"
      echo "-g, --graalvm=VERSION     specify GraalVM version to use, can be 20.1-dev or master, 20.1-dev by default"
      echo "-w, --workdir=/foo        specify the working directory, should be an absolute path, current one by default"
      echo "-p, --pull                force pulling of remote container images"
      echo "-r, --rebuild             force container image rebuild"
      exit 0
      ;;
    -j)
      shift
      if test $# -gt 0; then
        export JAVA_VERSION=$1
      else
        echo "no Java version specified"
        exit 1
      fi
      shift
      ;;
    --java*)
      export JAVA_VERSION=`echo $1 | sed -e 's/^[^=]*=//g'`
      shift
      ;;
    -g)
      shift
      if test $# -gt 0; then
        export GRAALVM_VERSION=$1
      else
        echo "no GraalVM version specified"
        exit 1
      fi
      shift
      ;;
    --graalvm*)
      export GRAALVM_VERSION=`echo $1 | sed -e 's/^[^=]*=//g'`
      shift
      ;;
    -w)
      shift
      if test $# -gt 0; then
        export HOST_WORK_DIR=$1
      else
        echo "no working directory specified"
        exit 1
      fi
      shift
      ;;
    --workdir)
      export HOST_WORK_DIR=`echo $1 | sed -e 's/^[^=]*=//g'`
      shift
      ;;
    -p)
      export PULL=true
      export REBUILD=true
      shift
      ;;
    --pull)
      export PULL=true
      export REBUILD=true
      shift
      ;;
    -r)
      export REBUILD=true
      shift
      ;;
    --rebuild)
      export REBUILD=true
      shift
      ;;
    *)
      break
      ;;
  esac
done

DOCKER_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/docker" >/dev/null 2>&1 && pwd )"
CONTAINER_HOME=/home/$USER
CONTAINER_WORK_DIR=$CONTAINER_HOME/spring-graalvm-native
DEV_IMAGE=$(test $OSTYPE = "darwin" && echo "Dockerfile.spring-graalvm-native-dev-mac" || echo "Dockerfile.spring-graalvm-native-dev")

if [ "$PULL" = true ] ; then
    echo "Updating container image if needed"
    docker pull springci/spring-graalvm-native:${GRAALVM_VERSION}-java${JAVA_VERSION}
fi

docker image ls | grep spring-graalvm-native-dev | grep ${GRAALVM_VERSION}-java${JAVA_VERSION} >/dev/null 2>&1 || export REBUILD=true

test "$REBUILD" = false || docker build \
  --build-arg BASE_IMAGE=springci/spring-graalvm-native:${GRAALVM_VERSION}-java${JAVA_VERSION} \
  --build-arg USER=$USER \
  --build-arg USER_ID=$(id -u ${USER}) \
  --build-arg USER_GID=$(id -g ${USER}) \
  -t spring-graalvm-native-dev:${GRAALVM_VERSION}-java${JAVA_VERSION} - < $DOCKER_DIR/$DEV_IMAGE

docker run --hostname docker -p 8080:8080 -v $HOST_WORK_DIR:$CONTAINER_WORK_DIR:delegated -v $HOME/.m2:$CONTAINER_HOME/.m2:delegated -it -w $CONTAINER_WORK_DIR -u $(id -u ${USER}):$(id -g ${USER}) spring-graalvm-native-dev:${GRAALVM_VERSION}-java${JAVA_VERSION}
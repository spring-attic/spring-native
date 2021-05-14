#!/usr/bin/env bash

MILESTONE=0.10.x
JAVA_VERSION=11
GRAALVM_VERSION=stable
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
      echo "-j, --java=VERSION        specify Java version to use, can be 8 or 11, 11 by default"
      echo "-g, --graalvm=VERSION     specify GraalVM flavor to use, can be stable or dev, stable by default"
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
CONTAINER_WORK_DIR=$CONTAINER_HOME/spring-native
CONTAINER_TAG=${GRAALVM_VERSION}-java${JAVA_VERSION}-${MILESTONE}
DEV_IMAGE=Dockerfile.spring-native-dev

if [ "$PULL" = true ] ; then
    echo "Updating container image if needed"
    docker pull springci/spring-native:${CONTAINER_TAG}
fi

docker image ls | grep spring-native-dev | grep ${CONTAINER_TAG} >/dev/null 2>&1 || export REBUILD=true

test "$REBUILD" = false || docker build \
  --build-arg BASE_IMAGE=springci/spring-native:${CONTAINER_TAG} \
  --build-arg USER=$USER \
  --build-arg USER_ID=$(id -u ${USER}) \
  --build-arg USER_GID=$(id -g ${USER}) \
  -t spring-native-dev:${CONTAINER_TAG} - < $DOCKER_DIR/$DEV_IMAGE

docker run --hostname docker -p 8080:8080 -v $HOST_WORK_DIR:$CONTAINER_WORK_DIR:delegated -v $HOME/.m2:$CONTAINER_HOME/.m2:delegated -it --privileged -w $CONTAINER_WORK_DIR spring-native-dev:${CONTAINER_TAG}
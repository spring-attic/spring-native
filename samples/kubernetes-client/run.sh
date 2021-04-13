#!/usr/bin/env bash

docker run --volume ${HOME}/.kube/config:/home/cnb/.kube/config docker.io/library/kubernetes-client-example:0.0.1-SNAPSHOT

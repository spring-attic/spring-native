#!/usr/bin/env bash

docker run -v `pwd`:/home -it --entrypoint /bin/bash springci/graalvm-ce-java8:20.1.0-dev_20200125-1203
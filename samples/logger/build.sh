#!/usr/bin/env bash

${PWD%/*samples/*}/scripts/compileWithMaven.sh $* &&  ${PWD%/*samples/*}/scripts/test.sh $* -Dlogging.config=src/main/resources/logback-config.xml
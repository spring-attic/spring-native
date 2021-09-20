#!/usr/bin/env bash

${PWD%/*samples/*}/scripts/compileWithMaven.sh $* -Dspring.native.build-time-properties-checks=default-include-all &&  ${PWD%/*samples/*}/scripts/test.sh $*

#!/usr/bin/env bash

#${PWD%/*samples/*}/scripts/compileWithMaven.sh -Dspring.native.build-time-properties-checks=default-include-all -Dspring.native.factories.no-actuator-metrics=true && ${PWD%/*samples/*}/scripts/test.sh
${PWD%/*samples/*}/scripts/compileWithMaven.sh -Dspring.native.build-time-properties-checks=default-include-all && ${PWD%/*samples/*}/scripts/test.sh

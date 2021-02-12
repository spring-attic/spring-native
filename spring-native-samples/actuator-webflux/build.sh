#!/usr/bin/env bash

${PWD%/*samples/*}/scripts/compileWithMaven.sh -Dspring.native.build-time-properties-checks=default-include-all -Dspring.native.build-time-properties-match-if-missing=false -Dspring.native.factories.no-actuator-metrics=true && ${PWD%/*samples/*}/scripts/test.sh

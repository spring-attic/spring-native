#!/usr/bin/env bash

${PWD%/*samples/*}/scripts/install3rdPartyHints.sh && ${PWD%/*samples/*}/scripts/compileWithMaven.sh $* &&  ${PWD%/*samples/*}/scripts/test.sh $*
#!/usr/bin/env bash

set -e

export DB=h2
${PWD%/*samples/*}/scripts/install3rdPartyHints.sh && ${PWD%/*samples/*}/scripts/compileWithMaven.sh $* &&  ${PWD%/*samples/*}/scripts/test.sh $*
export DB=hsqldb
${PWD%/*samples/*}/scripts/install3rdPartyHints.sh && ${PWD%/*samples/*}/scripts/compileWithMaven.sh $* &&  ${PWD%/*samples/*}/scripts/test.sh $*

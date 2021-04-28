#!/usr/bin/env bash

RC=0

${PWD%/*samples/*}/scripts/compileWithMaven.sh && ${PWD%/*samples/*}/scripts/test.sh || RC=$?

exit $RC

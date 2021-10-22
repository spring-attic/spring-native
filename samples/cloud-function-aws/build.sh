#!/usr/bin/env bash

RC=0

${PWD%/*samples/*}/scripts/compileWithMaven.sh $*

exit $RC
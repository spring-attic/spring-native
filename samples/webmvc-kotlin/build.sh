#!/usr/bin/env bash

BLUE='\033[0;34m'
NC='\033[0m'

${PWD%/*samples/*}/scripts/compileWithGradle.sh && ${PWD%/*samples/*}/scripts/test.sh

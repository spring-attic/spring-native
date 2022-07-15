#!/usr/bin/env bash

source ${PWD%/*samples/*}/scripts/wait.sh
wait_log target/native/test-output.txt "Received:(HOW|MUCH|WOOD|COULD|A|WOODCHUCK|CHUCK|IF|COULD|WOOD?)"

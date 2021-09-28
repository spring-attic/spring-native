#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_http localhost:9000/config-client/dev/master "from application-dev.yml"

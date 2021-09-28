#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_http localhost:8080/reservations '[{"name":"Andy","id":1},{"name":"Sebastien","id":2}]'

#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_http localhost:8080/reservations '[{"id":1,"name":"Andy"},{"id":2,"name":"Sebastien"}]'

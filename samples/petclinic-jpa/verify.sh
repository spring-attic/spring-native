#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_http localhost:8080 "Welcome"' ERR
trap 'wait_http http://localhost:8080/owners/7 "Jeff Black"' ERR
wait_http http://localhost:8080/vets.html "James Carter"


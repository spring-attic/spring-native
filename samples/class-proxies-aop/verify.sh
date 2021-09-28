#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_http localhost:8080/with "hello1"' ERR
wait_http localhost:8080/without "hello2"

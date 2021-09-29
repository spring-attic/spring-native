#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_http user:password@localhost:8080 "This page is secured"

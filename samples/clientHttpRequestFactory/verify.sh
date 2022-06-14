#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_log target/native/test-output.txt "RestTemplate: org.springframework.web.client.RestTemplate@"

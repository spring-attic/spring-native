#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_http localhost:8080/greeting "Hello, World!"' ERR
trap 'wait_http localhost:8080/greeting "<title>"' ERR
trap 'wait_http localhost:8080/greetings "<td>foo</td>"' ERR
wait_http localhost:8080/greetings "<td>bar</td>"

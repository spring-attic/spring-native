#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_http localhost:8080/ 'Hello from Spring MVC and Tomcat'
wait_http localhost:8080/foo.html 'Foo'
wait_http localhost:8080/bar.html 'Bar'

#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_http localhost:8080/ "Hello from Spring MVC and Tomcat" || RC=$?
wait_http localhost:8080/foo.html "Foo" || RC=$?
wait_http localhost:8080/bar.html "Bar" || RC=$?
wait_http_post localhost:8080/echo '{"message": "Native"}' 'Native' || RC=$?
wait_http localhost:8080/record '{"message":"Hello from Spring MVC and Tomcat"}' || RC=$?

exit $RC

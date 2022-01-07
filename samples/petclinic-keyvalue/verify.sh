#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_http localhost:8080 "Welcome" || RC=$?
wait_http http://localhost:8080/owners/7 "Jeff Black" || RC=$?
wait_http http://localhost:8080/vets.html "James Carter" || RC=$?

exit $RC

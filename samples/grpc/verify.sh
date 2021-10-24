#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
RC=0

wait_command_output "grpcurl -plaintext localhost:50051 describe demo.Greeter 2>&1" "demo.Greeter is a service:" || RC=$?
wait_command_output "grpcurl -plaintext -d '{}' localhost:50051 demo.Greeter/Hello 2>&1" "\"firstName\": \"Josh\"," || RC=$?

exit $RC

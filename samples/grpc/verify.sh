#!/usr/bin/env bash
OUTPUT=`grpcurl -plaintext localhost:50051 describe demo.Greeter 2>&1`
if [[ ${OUTPUT} == *"demo.Greeter is a service:"* ]]; then
  OUTPUT=`grpcurl -plaintext -d '{}' localhost:50051 demo.Greeter/Hello 2>&1`
  if [[ ${OUTPUT} == *"\"firstName\": \"Josh\","* ]]; then
    exit 0
  fi
fi
exit 1

#!/usr/bin/env bash
if [[ `rsc --debug --request --data "{\"origin\":\"Client\",\"interaction\":\"Request\"}" --route request-response tcp://localhost:7000 | grep "Frame => Stream ID: 1 Type: NEXT_COMPLETE Flags: 0b1100000 Length: 81"` ]]; then
  if [[ `rsc --debug --request --data "{\"origin\":\"Client\",\"interaction\":\"Request\"}" --route mono-request-response tcp://localhost:7000 | grep "Frame => Stream ID: 1 Type: NEXT_COMPLETE Flags: 0b1100000 Length: 81"` ]]; then
    exit 0
  fi
else
  exit 1
fi

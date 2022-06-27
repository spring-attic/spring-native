#!/usr/bin/env bash

set -euo pipefail

BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

printf "=== ${BLUE}Building 3rd party hints${NC} ===\n"

if ! mvn -ntp clean install --file "${SCRIPT_DIR}"/../3rd-party-hints/pom.xml &> target/native/3rd-party-samples-output.txt; then
  printf "${RED}Building 3rd party hints failed!${NC}\n"
  cat target/native/3rd-party-samples-output.txt
  exit 1
fi

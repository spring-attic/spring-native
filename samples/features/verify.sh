#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
trap 'wait_log target/native/test-output.txt "commandlinerunner running!"' ERR
wait_log target/native/test-output.txt "ApplicationContextAware callback invoked"
wait_log target/native/test-output.txt "lazyBean order: \[before, created, after\]"
wait_log target/native/test-output.txt "lazyProviderBean order: \[before-bean, before-provider, created, after-provider, holder-created, after-bean\]"

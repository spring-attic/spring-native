#!/usr/bin/env bash
sleep 2
if [[ `cat target/native/test-output.txt | grep "CURRENT INTEGRATION GRAPH:"` ]]; then
	exit 0
else
	exit 1
fi

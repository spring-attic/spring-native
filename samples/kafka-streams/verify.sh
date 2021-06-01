#!/usr/bin/env bash
sleep 2
if [[ `cat target/native/test-output.txt | grep "++++++Received:FOO"` ]]; then
	exit 0
else
	exit 1
fi

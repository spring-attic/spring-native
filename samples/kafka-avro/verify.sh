#!/usr/bin/env bash
sleep 2
if [ `cat target/native-image/test-output.txt | grep '++++++Received Thing' | wc -l` -eq 3 ]; then
	exit 0
else
	exit 1
fi

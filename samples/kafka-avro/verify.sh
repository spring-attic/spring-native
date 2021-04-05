#!/usr/bin/env bash
sleep 2
if [[ `cat target/native-image/test-output.txt | grep '++++++Received:{"stringField": "someValue", "intField": 42}'` ]]; then
	exit 0
else
	exit 1
fi

#!/usr/bin/env bash
sleep 2
if [[ `cat target/native/test-output.txt | grep -E "Received:(HOW|MUCH|WOOD|A|WOODCHUCK|CHUCK|IF|COULD|WOULD?)"` ]]; then
	exit 0
else
	exit 1
fi
#!/usr/bin/env bash
LINES=`cat target/native-image/test-output.txt | grep "jafu running!" | wc -l`
exit `expr 1 - $LINES`

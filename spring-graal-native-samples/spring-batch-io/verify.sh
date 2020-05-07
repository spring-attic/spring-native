#!/usr/bin/env bash
file1="src/test/resources/output.csv"
file2=$1

if cmp -s "$file1" "$file2"; then
    exit 0
else
    exit 1
fi

#!/usr/bin/env bash
rm -rf target/
mkdir -p target/native-image 2>/dev/null
mvn -Pgraal package > target/native-image/output.txt

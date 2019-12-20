#!/usr/bin/env bash

echo "Graal: `native-image --version`" > samples-summary.csv
echo "Date,Sample,Image Build Time (ms),Image Build Memory (GB),RSS Memory (M),Image Size (M),Startup Time (s),JVM Uptime (s)" >> samples-summary.csv
for d in spring-graal-native-samples/*/
do
  if [[ -f "$d/build.sh" ]]; then
    (cd "$d" && ./build.sh)
    if [[ -f "$d/target/native-image/summary.csv" ]]; then
      cat $d/target/native-image/summary.csv >> samples-summary.csv
    fi 
  fi
done

#!/usr/bin/env bash

echo "Graal: `native-image --version`" > samples-summary.csv
echo "Date,Sample,Image Build Time (ms),Image Build Memory (GB),RSS Memory (M),Image Size (M),Startup Time (s),JVM Uptime (s)" >> samples-summary.csv
for i in commandlinerunner webflux-netty springmvc-tomcat jafu jafu-webmvc
do
  (cd spring-graal-native-samples/$i && ./build.sh && cat ./target/native-image/summary.csv >> ../../samples-summary.csv)
done

head -1 samples-summary.csv
if ! [ -x "$(command -v tty-table)" ]; then
  tail +2 samples-summary.csv | perl -pe 's/((?<=,)|(?<=^)),/ ,/g;'  | column -t -s, 
else
  tail +2 samples-summary.csv | tty-table
fi

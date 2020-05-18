#!/usr/bin/env bash

RC=0

echo "Graal: `native-image --version`" > samples-summary.csv
echo "Date,Sample,Build Time (s),Build Mem (GB),RSS Mem (M),Image Size (M),Startup Time (s),JVM Uptime (s)" >> samples-summary.csv
for i in commandlinerunner webflux-netty springmvc-tomcat jafu jafu-webmvc vanilla-thymeleaf vanilla-grpc vanilla-tx
do

  if ! (cd spring-graalvm-native-samples/$i && ./build.sh); then
    RC=1
  fi
  if [ -f "spring-graalvm-native-samples/$i/target/native-image/summary.csv" ]; then
    cat spring-graalvm-native-samples/$i/target/native-image/summary.csv >> samples-summary.csv
  else
    echo `date +%Y%m%d-%H%M`,$i,ERROR,,,,, >> samples-summary.csv
  fi

  if [ -d "spring-graalvm-native-samples/$i/agent" ]; then
    (cd spring-graalvm-native-samples/$i/agent && ./build.sh)
    if [ -f "spring-graalvm-native-samples/$i/agent/target/native-image/summary.csv" ]; then
      cat spring-graalvm-native-samples/$i/agent/target/native-image/summary.csv >> samples-summary.csv
    else
      echo `date +%Y%m%d-%H%M`,${i}-agent,ERROR,,,,, >> samples-summary.csv
    fi
  fi
done

head -1 samples-summary.csv
if ! [ -x "$(command -v tty-table)" ]; then
  tail -n +2 samples-summary.csv | perl -pe 's/((?<=,)|(?<=^)),/ ,/g;'  | column -t -s, 
else
  tail -n +2 samples-summary.csv | tty-table
fi

exit $RC

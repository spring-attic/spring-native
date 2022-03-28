#!/usr/bin/env bash

RC=0

# Uses . as decimal separator
export LANG=C

echo "GraalVM: `native-image --version`" > samples-summary.csv
echo "Date,Sample,Build Time (s),Build Mem (GB),RSS Mem (M),Image Size (M),Startup Time (s),JVM Uptime (s), ReflectConfig (lines)" >> samples-summary.csv
for i in commandlinerunner webflux-netty webmvc-tomcat webflux-thymeleaf grpc jdbc-tx class-proxies-aop batch
do

  if ! (cd samples/$i && ./build.sh); then
    RC=1
  fi
  if [ -f "samples/$i/target/native/summary.csv" ]; then
    cat samples/$i/target/native/summary.csv >> samples-summary.csv
  else
    echo `date +%Y%m%d-%H%M`,$i,ERROR,,,,,, >> samples-summary.csv
  fi

done

head -1 samples-summary.csv
if ! [ -x "$(command -v tty-table)" ]; then
  tail -n +2 samples-summary.csv | perl -pe 's/((?<=,)|(?<=^)),/ ,/g;'  | column -t -s, 
else
  tail -n +2 samples-summary.csv | tty-table
fi

exit $RC

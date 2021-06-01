#!/usr/bin/env bash

RC=0

#TODO Disabled due to Docker rate limit issue, to be restored when https://github.com/spring-projects/spring-boot/issues/25898 will be fixed
#echo "Testing buildpacks-based builds"
#if ! (cd "samples/commandlinerunner" && mvn -ntp spring-boot:build-image); then
#  RC=1
#fi
#docker run commandlinerunner:0.0.1-SNAPSHOT&
#PID=$!
#sleep 3
#kill ${PID} > /dev/null 2>&1
#if ! (cd "samples/commandlinerunner-gradle" && ./gradlew bootBuildImage); then
#  RC=1
#fi
#docker run commandlinerunner-gradle:0.0.1-SNAPSHOT&
#PID=$!
#sleep 3
#kill ${PID} > /dev/null 2>&1
#if ! (cd "samples/webmvc-kotlin" && ./gradlew bootBuildImage); then
#  RC=1
#fi
##docker run webmvc-kotlin:0.0.1-SNAPSHOT&
#PID=$!
#sleep 3
#kill ${PID} > /dev/null 2>&1
#if ! (cd "samples/security-kotlin" && ./gradlew bootBuildImage); then
#  RC=1
#fi
#docker run security-kotlin:0.0.1-SNAPSHOT&
#PID=$!
#sleep 3
#kill ${PID} > /dev/null 2>&1


echo "GraalVM: `native-image --version`" > samples-summary.csv
echo "Date,Sample,Build Time (s),Build Mem (GB),RSS Mem (M),Image Size (M),Startup Time (s),JVM Uptime (s)" >> samples-summary.csv
for d in $(find samples -maxdepth 2 -type d)
do
  if [[ -f "$d/build.sh" && ! -f "$d/.ignore" ]]; then
    if ! (cd "$d" && ./build.sh); then
      RC=1
    fi
    if [ -f "$d/target/native/summary.csv" ]; then
      cat $d/target/native/summary.csv >> samples-summary.csv
    else
     echo `date +%Y%m%d-%H%M`,`basename $d`,ERROR,-,,,, >> samples-summary.csv
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

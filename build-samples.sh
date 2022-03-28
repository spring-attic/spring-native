#!/usr/bin/env bash

RC=0

# Uses . as decimal separator
export LANG=C

echo "Testing buildpacks-based builds"
if ! (cd "samples/commandlinerunner" && mvn -ntp clean package spring-boot:build-image); then
  RC=1
fi
docker run commandlinerunner:0.0.1-SNAPSHOT&
PID=$!
sleep 3
kill ${PID} > /dev/null 2>&1
if ! (cd "samples/commandlinerunner-gradle" && ./gradlew bootBuildImage); then
  RC=1
fi
docker run commandlinerunner-gradle:0.0.1-SNAPSHOT&
PID=$!
sleep 3
kill ${PID} > /dev/null 2>&1
if ! (cd "samples/webmvc-kotlin" && ./gradlew bootBuildImage); then
  RC=1
fi
docker run webmvc-kotlin:0.0.1-SNAPSHOT&
PID=$!
sleep 3
kill ${PID} > /dev/null 2>&1
if ! (cd "samples/security-kotlin" && ./gradlew bootBuildImage); then
  RC=1
fi
docker run security-kotlin:0.0.1-SNAPSHOT&
PID=$!
sleep 3
kill ${PID} > /dev/null 2>&1

echo "GraalVM: `native-image --version`" > samples-summary.csv
echo "Date,Sample,Build Time (s),Build Mem (GB),RSS Mem (M),Image Size (M),Startup Time (s),JVM Uptime (s),ReflectConfig (lines)" >> samples-summary.csv
for d in $(find samples -maxdepth 2 -type d | sort -n)
do
  if [[ -f "$d/build.sh" && ! -f "$d/.ignore" ]]; then
    if ! (cd "$d" && ./build.sh); then
      RC=1
    fi
    if [ -f "$d/pom.xml" ]; then
      REPORT_DIR="$d/target/native"
    else
      REPORT_DIR="$d/build/native"
    fi
    if [ -f "$REPORT_DIR/summary.csv" ]; then
      cat $REPORT_DIR/summary.csv >> samples-summary.csv
    else
     echo `date +%Y%m%d-%H%M`,`basename $d`,ERROR,,,,,, >> samples-summary.csv
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

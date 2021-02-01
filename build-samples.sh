#!/usr/bin/env bash

RC=0

# To skip agent builds on Java 8 due to https://github.com/oracle/graal/issues/3010
if java -version 2>&1 | grep "openjdk version \"1.8.0"; then
    MAX_DEPTH=1
else
    MAX_DEPTH=2
fi

echo "Testing buildpacks-based builds"
if ! (cd "spring-native-samples/commandlinerunner" && mvn spring-boot:build-image); then
  RC=1
fi
if ! (cd "spring-native-samples/webmvc-kotlin" && ./gradlew bootBuildImage); then
  RC=1
fi

echo "GraalVM: `native-image --version`" > samples-summary.csv
echo "Date,Sample,Build Time (s),Build Mem (GB),RSS Mem (M),Image Size (M),Startup Time (s),JVM Uptime (s)" >> samples-summary.csv
for d in $(find spring-native-samples -maxdepth $MAX_DEPTH -type d)
do
  if [[ -f "$d/build.sh" && ! -f "$d/.ignore" ]]; then
    if ! (cd "$d" && ./build.sh); then
      RC=1
    fi
    if [ -f "$d/target/native-image/summary.csv" ]; then
      cat $d/target/native-image/summary.csv >> samples-summary.csv
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

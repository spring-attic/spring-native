#!/usr/bin/env bash

RC=0

# Uses . as decimal separator
export LANG=C

if [ -f samples-summary.csv ]; then
  rm samples-summary.csv
fi

echo "Date,Sample,Build Time (s),Build Mem (GB),RSS Mem (M),Image Size (M),Startup Time (s),JVM Uptime (s),ReflectConfig (lines)" >> samples-summary.csv
for d in $(find samples -maxdepth 2 -type d | sort -n)
do
  if [[ -f "$d/build.sh" && ! -f "$d/.ignore" ]]; then
    if ! (cd "$d" && ./build.sh --aot-only); then
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

if ! [ -x "$(command -v tty-table)" ]; then
  tail -n +1 samples-summary.csv | perl -pe 's/((?<=,)|(?<=^)),/ ,/g;'  | column -t -s,
else
  tail -n +1 samples-summary.csv | tty-table
fi

exit $RC

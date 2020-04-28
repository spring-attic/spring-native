#!/usr/bin/env bash
# If supplied $1 is the executable and $2 is the file where to create test-output.txt and summary.csv, otherwise these
# default to the 'target/current-directory-name' (e.g. target/commandlinerunner) and 
# target/native-image respectively

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

if [[ "$1" == "-s" ]]; then
  SILENT=true
  shift 1
else 
  SILENT=false
fi

if ! [ -z "$1" ]; then
	if [[ "$1" != "--"* ]]; then
		EXECUTABLE=${1}
		shift 1
	else
		EXECUTABLE=target/${PWD##*/}
	fi
else
	EXECUTABLE=${1:-target/${PWD##*/}}
fi

if [ -z "$1" ] || [[ "$1" == "--"* ]]; then
  TEST_OUTPUT_FILE=target/native-image/test-output.txt
  BUILD_OUTPUT_FILE=target/native-image/output.txt
  SUMMARY_CSV_FILE=target/native-image/summary.csv
else
  TEST_OUTPUT_FILE=$1/test-output.txt
  BUILD_OUTPUT_FILE=$1/output.txt
  SUMMARY_CSV_FILE=$1/summary.csv
  shift 1
fi
echo "Testing executable '`basename $EXECUTABLE`'"

chmod +x ${EXECUTABLE}
./${EXECUTABLE} "$@" > $TEST_OUTPUT_FILE 2>&1 &
PID=$!
sleep 3

#if [[ `cat $TEST_OUTPUT_FILE | grep "commandlinerunner running!"` ]]
./verify.sh
RC=$?
if [[ $RC == 0 ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
  if [ $SILENT == 'false' ]; then
    TOTALINFO=`cat $BUILD_OUTPUT_FILE | grep "\[total\]"`
    BUILDTIME=`echo $TOTALINFO | sed 's/^.*\[total\]: \(.*\) ms.*$/\1/' | tr -d -c 0-9\.`
    BUILDTIME=`bc <<< "scale=1; ${BUILDTIME}/1024"`
    BUILDMEMORY=`echo $TOTALINFO | grep GB | sed 's/^.*\[total\]: .* ms,\(.*\) GB$/\1/' | tr -d -c 0-9\.`
    if [ -z "$BUILDMEMORY" ]; then
      BUILDMEMORY="-"
    fi
    echo "Build memory: ${BUILDMEMORY}GB"
    echo "Image build time: ${BUILDTIME}s"
    RSS=`ps -o rss ${PID} | tail -n1`
    RSS=`bc <<< "scale=1; ${RSS}/1024"`
    echo "RSS memory: ${RSS}M"
    SIZE=`wc -c <"${EXECUTABLE}"`/1024
    SIZE=`bc <<< "scale=1; ${SIZE}/1024"`
    echo "Image size: ${SIZE}M"
    STARTUP=`cat $TEST_OUTPUT_FILE | grep "JVM running for"`
    REGEXP="Started .* in ([0-9\.]*) seconds \(JVM running for ([0-9\.]*)\).*$"
    if [[ ${STARTUP} =~ ${REGEXP} ]]; then
      STIME=${BASH_REMATCH[1]}
      JTIME=${BASH_REMATCH[2]}
      echo "Startup time: ${STIME} (JVM running for ${JTIME})"
    fi
    echo `date +%Y%m%d-%H%M`,`basename $EXECUTABLE`,$BUILDTIME,$BUILDMEMORY,${RSS},${SIZE},${STIME},${JTIME}  > $SUMMARY_CSV_FILE
  fi
  if ! kill ${PID} > /dev/null 2>&1; then
    echo "Did not kill process, it ended on it's own" >&2
  fi
  exit 0
else
  cat $BUILD_OUTPUT_FILE
  cat $TEST_OUTPUT_FILE
  printf "${RED}FAILURE${NC}: the output of the application does not contain the expected output\n"
  if [ $SILENT == 'false' ]; then
    echo `date +%Y%m%d-%H%M`,`basename $EXECUTABLE`,ERROR,-,,,, > $SUMMARY_CSV_FILE
  fi
  if ! kill ${PID} > /dev/null 2>&1; then
    echo "Did not kill process, it ended on it's own" >&2
  fi
  exit 1
fi

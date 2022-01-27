#!/usr/bin/env bash
# If supplied $1 is the executable and $2 is the file where to create test-output.txt and summary.csv, otherwise these
# default to the 'target/current-directory-name' (e.g. target/commandlinerunner) and target/native respectively for Maven
# or 'build/native/current-directory-name' (e.g. build/native/commandlinerunner) and build/native respectively for Gradle.

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

AOT_ONLY=false
SILENT=false

while test $# -gt 0; do
  case "$1" in
    -a)
      export AOT_ONLY=true
      shift
      ;;
    --aot-only)
      export AOT_ONLY=true
      shift
      ;;
    -s)
      export SILENT=true
      shift
      ;;
    -t)
      shift
      ;;
    --native-tests)
      shift
      ;;
    *)
      break
      ;;
  esac
done

if [ -f pom.xml ]; then
  EXECUTABLE_DIR=target
  JAR_DIR=target
  GENERATED_DIR=target/generated-runtime-sources/spring-aot/src/main/resources
  REPORT_DIR=target/native
else
  EXECUTABLE_DIR=build/native/nativeCompile
  JAR_DIR=build/libs
  GENERATED_DIR=build/generated/resources/aotMain
  REPORT_DIR=build/native
fi

if [[ "$AOT_ONLY" == true ]]; then
  EXECUTABLE="java -DspringAot=true -jar $JAR_DIR/*-SNAPSHOT.jar"
else
  if ! [ -z "$1" ]; then
    if [[ "$1" != "--"* ]]; then
      EXECUTABLE=${1}
      shift 1
    else
      EXECUTABLE=${EXECUTABLE_DIR}/${PWD##*/}
    fi
  else
    EXECUTABLE=${1:-${EXECUTABLE_DIR}/${PWD##*/}}
  fi
  chmod +x ${EXECUTABLE}
fi

if [ -z "$1" ] || [[ "$1" == "--"* ]]; then
  TEST_OUTPUT_FILE=${REPORT_DIR}/test-output.txt
  BUILD_OUTPUT_FILE=${REPORT_DIR}/output.txt
  SUMMARY_CSV_FILE=${REPORT_DIR}/summary.csv
else
  TEST_OUTPUT_FILE=$1/test-output.txt
  BUILD_OUTPUT_FILE=$1/output.txt
  SUMMARY_CSV_FILE=$1/summary.csv
  shift 1
fi
echo "Testing `basename ${PWD##*/}`"

if [[ "$AOT_ONLY" == true ]]; then
  ${EXECUTABLE} "$@" > $TEST_OUTPUT_FILE 2>&1 &
else
  ./${EXECUTABLE} "$@" > $TEST_OUTPUT_FILE 2>&1 &
fi
PID=$!

VERIFY_RESULT=$(./verify.sh 2>&1)
RC=$?
if [[ $RC == 0 ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
  if [ $SILENT == 'false' ]; then
    if [[ "$AOT_ONLY" == false ]]; then
      BUILDTIME=`cat $BUILD_OUTPUT_FILE | grep "Finished generating" | sed 's/Finished generating '.*' in \(.*\)s./\1/'`
      BUILDMEMORY=`cat $BUILD_OUTPUT_FILE | grep "Peak RSS: " | sed 's/.*Peak RSS: \(.*\)GB | .*/\1/'`
    fi
    if [ ! -z "$BUILDMEMORY" ]; then
      echo "Build memory: ${BUILDMEMORY}GB"
    fi
    if [ ! -z "$BUILDTIME" ]; then
      echo "Image build time: ${BUILDTIME}s"
    fi
    RSS=`ps -o rss ${PID} | tail -n1`
    RSS=`bc <<< "scale=1; ${RSS}/1024"`
    echo "RSS memory: ${RSS}M"
    if [[ "$AOT_ONLY" == false ]]; then
      SIZE=`wc -c <"${EXECUTABLE}"`/1024
      SIZE=`bc <<< "scale=1; ${SIZE}/1024"`
      echo "Image size: ${SIZE}M"
    fi
    STARTUP=`cat $TEST_OUTPUT_FILE | grep "JVM running for"`
    REGEXP="Started .* in ([0-9\.]*) seconds \(JVM running for ([0-9\.]*)\).*$"
    if [[ ${STARTUP} =~ ${REGEXP} ]]; then
      STIME=${BASH_REMATCH[1]}
      JTIME=${BASH_REMATCH[2]}
      echo "Startup time: ${STIME} (JVM running for ${JTIME})"
    fi
    if [[ ${PWD##*/} != *-agent ]] ; then
      CONFIGLINES=`wc -l $GENERATED_DIR/META-INF/native-image/org.springframework.aot/spring-aot/reflect-config.json | sed 's/^ *//g' | cut -d" " -f1`
        echo "Lines of reflective config: $CONFIGLINES"
      else
        CONFIGLINES=0
    fi
    echo `date +%Y%m%d-%H%M`,`basename ${PWD##*/}`,$BUILDTIME,$BUILDMEMORY,${RSS},${SIZE},${STIME},${JTIME},${CONFIGLINES}  > $SUMMARY_CSV_FILE
  fi
  if ! kill ${PID} > /dev/null 2>&1; then
    echo "Did not kill process, it ended on it's own" >&2
  fi
  exit 0
else
  cat $BUILD_OUTPUT_FILE
  cat $TEST_OUTPUT_FILE
  printf "${RED}FAILURE${NC}: the output of the application does not contain the expected output\n"
  printf "${VERIFY_RESULT}\n"
  if [ $SILENT == 'false' ]; then
    echo `date +%Y%m%d-%H%M`,`basename ${PWD##*/}`,ERROR,-,,,,, > $SUMMARY_CSV_FILE
  fi
  if ! kill ${PID} > /dev/null 2>&1; then
    echo "Did not kill process, it ended on it's own" >&2
  fi
  exit 1
fi

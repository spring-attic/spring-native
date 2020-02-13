#!/usr/bin/env bash

ARTIFACT=springmvc-undertow
MAINCLASS=com.example.undertow.UndertowApplication
VERSION=0.0.1-SNAPSHOT
FEATURE=../../../../spring-graal-native-feature/target/spring-graal-native-feature-0.6.0.BUILD-SNAPSHOT.jar

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

rm -rf target
mkdir -p target/native-image

echo "Packaging $ARTIFACT with Maven"
mvn -DskipTests package > target/native-image/output.txt

JAR="$ARTIFACT-$VERSION.jar"
rm -f $ARTIFACT
echo "Unpacking $JAR"
cd target/native-image
jar -xvf ../$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

LIBPATH=`find BOOT-INF/lib | tr '\n' ':'`
CP=BOOT-INF/classes:$LIBPATH:$FEATURE

echo "Generating reflection files for $ARTIFACT"
rm -rf graal/META-INF 2>/dev/null
mkdir -p graal/META-INF/native-image
java -agentlib:native-image-agent=config-output-dir=graal/META-INF/native-image -cp $CP $MAINCLASS >> output.txt 2>&1 &
PID=$!
sleep 3
curl -m 1 http://localhost:8080 > /dev/null 2>&1
sleep 1 && kill $PID || kill -9 $PID

GRAALVM_VERSION=`native-image --version`
echo "Compiling $ARTIFACT with $GRAALVM_VERSION in `which native-image`"
{ time native-image \
  -H:+AllowVMInspection \
  -H:+UseServiceLoaderFeature \
  -H:+TraceServiceLoaderFeature \
  --initialize-at-build-time=io.undertow.servlet.spec.ServletPrintWriterDelegate \
  --verbose \
  --no-server \
  -H:EnableURLProtocols=http,jar \
  -H:ReflectionConfigurationFiles=../../undertow-reflection.json,graal/META-INF/native-image/reflect-config.json \
  -H:ResourceConfigurationFiles=../../undertow-resource.json,graal/META-INF/native-image/resource-config.json \
  -H:DynamicProxyConfigurationResources=graal/META-INF/native-image/proxy-config.json \
  -H:+TraceClassInitialization \
  -H:IncludeResourceBundles=javax.servlet.http.LocalStrings \
  -H:Name=$ARTIFACT \
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
  -Dsun.rmi.transport.tcp.maxConnectionThreads=0 \
  -DremoveUnusedAutoconfig=true \
  -DremoveYamlSupport=true \
  -cp $CP $MAINCLASS >> output.txt ; } 2>> output.txt

if [[ -f $ARTIFACT ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
  mv ./$ARTIFACT ..
  exit 0
else
  cat output.txt
  printf "${RED}FAILURE${NC}: an error occurred when compiling the native-image.\n"
  exit 1
fi

#!/usr/bin/env bash

ARTIFACT=jafu-webmvc
MAINCLASS=com.example.jafu.JafuApplication
VERSION=0.0.1-SNAPSHOT

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

rm -rf target
mkdir -p target/native-image

echo "Packaging $ARTIFACT with Maven"
../../mvnw -DskipTests package > target/native-image/output.txt

JAR="$ARTIFACT-$VERSION.jar"
rm -f $ARTIFACT
echo "Unpacking $JAR"
cd target/native-image
jar -xvf ../$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

find BOOT-INF/lib | grep 19.3.0 | xargs rm

LIBPATH=`find BOOT-INF/lib | tr '\n' ':'`
CP=BOOT-INF/classes:$LIBPATH

GRAALVM_VERSION=`native-image --version`
echo "Compiling $ARTIFACT with $GRAALVM_VERSION"
time native-image \
  --no-server \
  --no-fallback \
  -H:+JNI \
  -H:EnableURLProtocols=http,https,jar \
  --initialize-at-build-time \
  --initialize-at-run-time=org.springframework.core.io.VfsUtils,org.apache.tomcat.jni.SSL \
  -H:ReflectionConfigurationFiles=../../tomcat-reflection.json,../../reflection-config.json -H:ResourceConfigurationFiles=../../tomcat-resource.json -H:JNIConfigurationFiles=../../tomcat-jni.json \
  --enable-https \
  -Dsun.rmi.transport.tcp.maxConnectionThreads=0 \
  -H:IncludeResourceBundles=javax.servlet.http.LocalStrings \
  -H:Name=$ARTIFACT \
  -H:+ReportExceptionStackTraces \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
  -cp $CP $MAINCLASS >> output.txt

if [[ -f $ARTIFACT ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
  mv ./$ARTIFACT ..
  exit 0
else
  printf "${RED}FAILURE${NC}: an error occurred when compiling the native-image.\n"
  exit 1
fi


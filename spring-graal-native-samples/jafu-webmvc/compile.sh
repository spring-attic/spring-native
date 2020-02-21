#!/usr/bin/env bash

ARTIFACT=jafu-webmvc
MAINCLASS=com.example.jafu.JafuApplication
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

# Avoids clashing substitutions from this project deps and the feature deps
rm BOOT-INF/lib/svm-19.*.jar

LIBPATH=`find BOOT-INF/lib | tr '\n' ':'`
CP=BOOT-INF/classes:$LIBPATH:$FEATURE

GRAALVM_VERSION=`native-image --version`
echo "Compiling $ARTIFACT with $GRAALVM_VERSION"
{ time native-image \
  --verbose \
  -Dmode=light \
  --no-server \
  --no-fallback \
  -H:EnableURLProtocols=http \
  --initialize-at-build-time \
  --initialize-at-run-time=org.springframework.core.io.VfsUtils,org.apache.tomcat.jni.SSL \
  -H:ReflectionConfigurationFiles=../../reflection-config.json \
  -H:Name=$ARTIFACT \
  -H:+ReportExceptionStackTraces \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
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

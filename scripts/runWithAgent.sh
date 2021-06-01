#!/usr/bin/env bash

ARTIFACT=`basename $PWD`
#MAINCLASS=app.main.SampleApplication
MAINCLASS=`grep -ri SpringApplication.run src | sed 's/^src\/main\/java\/\([^:]*\).java:.*$/\1/' | sed 's/\//\./g'`
VERSION=0.0.1.BUILD-SNAPSHOT

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

rm -rf target
mkdir -p target/native

echo "Packaging $ARTIFACT with Maven"
mvn -DskipTests package > target/native/output.txt

JAR="$ARTIFACT-$VERSION.jar"
rm -f $ARTIFACT
echo "Unpacking $JAR"
cd target/native
jar -xvf ../$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

LIBPATH=`find BOOT-INF/lib | tr '\n' ':'`
CP=BOOT-INF/classes:$LIBPATH

rm -rf graal/META-INF 2>/dev/null
mkdir -p graal/META-INF/native-image
java -agentlib:native-image-agent=config-output-dir=graal/META-INF/native-image -cp $CP $MAINCLASS

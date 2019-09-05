#!/usr/bin/env bash
export EXECUTABLE_NAME=grpc

../../mvnw -DskipTests clean package

export JAR=`ls -1 target/*.jar`

rm $EXECUTABLE_NAME
printf "Unpacking $JAR"
rm -rf unpack
mkdir unpack
cd unpack
jar -xvf ../$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

cd BOOT-INF/classes
export LIBPATH=`find ../../BOOT-INF/lib | tr '\n' ':'`
export CP=.:$LIBPATH

# Our feature being on the classpath is what triggers it
export CP=$CP:../../../../../target/spring-graal-feature-0.5.0.BUILD-SNAPSHOT.jar

printf "\n\nCompile\n"
native-image \
  -Dio.netty.noUnsafe=true \
  --no-server \
  -H:Name=$EXECUTABLE_NAME \
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
  -DremoveUnusedAutoconfig=true \
  -cp $CP com.example.ProtoApplication
  #--debug-attach \

mv $EXECUTABLE_NAME ../../..

printf "\n\nCompiled app...\n"
cd ../../..
time ./$EXECUTABLE_NAME


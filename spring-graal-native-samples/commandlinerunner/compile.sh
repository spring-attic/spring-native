#!/usr/bin/env bash
../../mvnw clean install

export JAR="commandlinerunner-0.0.1-SNAPSHOT.jar"
rm -f clr
printf "Unpacking $JAR"
rm -rf unpack
mkdir unpack
cd unpack
jar -xvf ../target/$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

cd BOOT-INF/classes
export LIBPATH=`find ../../BOOT-INF/lib | tr '\n' ':'`
export CP=.:$LIBPATH

# Our feature being on the classpath is what triggers it
export CP=$CP:../../../../../spring-graal-native-feature/target/spring-graal-native-feature-0.6.0.BUILD-SNAPSHOT.jar

printf "\n\nCompile\n"
native-image \
  -Dio.netty.noUnsafe=true \
  -Djava.awt.headless=true \
  --no-server \
  -H:+TraceClassInitialization \
  -H:Name=clr \
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
  -DremoveUnusedAutoconfig=true \
  -cp $CP com.example.commandlinerunner.CommandlinerunnerApplication

mv clr ../../..

printf "\n\nCompiled app (clr)\n"
cd ../../..
time ./clr


#!/usr/bin/env bash
../../mvnw -DskipTests clean package

export JAR="vanilla-thymeleaf-0.1.0.jar"
rm thymeleaf
printf "Unpacking $JAR"
rm -rf unpack
mkdir unpack
cd unpack
jar -xvf ../target/$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

cd BOOT-INF/classes
export LIBPATH=`find ../../BOOT-INF/lib | tr '\n' ':'`
export CP=.:$LIBPATH

# This would run it here... (as an exploded jar)
#java -classpath $CP hello.Application

# Our feature being on the classpath is what triggers it
export CP=$CP:../../../../../target/spring-boot-graal-feature-0.5.0.BUILD-SNAPSHOT.jar

printf "\n\nCompile\n"
native-image \
  -Dio.netty.noUnsafe=true \
  --no-server \
  -H:Name=thymeleaf \
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
  -H:+TraceClassInitialization=io.netty.bootstrap.AbstractBootstrap \
  -DremoveUnusedAutoconfig=true \
  -cp $CP hello.Application

  #--debug-attach \
mv thymeleaf ../../..

printf "\n\nCompiled app (demo)\n"
cd ../../..
./thymeleaf 


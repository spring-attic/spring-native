#!/usr/bin/env bash
../../mvnw clean install

export JAR="messages-0.0.1-SNAPSHOT.jar"
rm -f msgs
printf "Unpacking $JAR"
rm -rf unpack
mkdir unpack
cd unpack
jar -xvf ../target/$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

cd BOOT-INF/classes
export LIBPATH=`find ../../BOOT-INF/lib | tr '\n' ':'`
export CP=.:$LIBPATH


printf "\n\nCompile\n"
native-image \
  -Dio.netty.noUnsafe=true \
  --no-server \
  -H:Name=msgs \
  -H:+TraceClassInitialization \
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
  --initialize-at-run-time=org.springframework.context.annotation.FilterType,org.springframework.context.annotation.ScopedProxyMode,org.springframework.core.annotation.AnnotationFilter \
  -cp $CP com.example.messages.MessagesApplication

mv msgs ../../..

printf "\n\nJava exploded jar\n"
time java -classpath $CP com.example.messages.MessagesApplication

printf "\n\nCompiled app (logger)\n"
cd ../../..
time ./msgs


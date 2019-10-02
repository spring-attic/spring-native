#!/usr/bin/env bash
../../mvnw clean install

export JAR="logger-0.0.1-SNAPSHOT.jar"
rm -f logger
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
export CP=$CP:../../../../../spring-graal-native-image-feature/target/spring-graal-native-image-feature-0.6.0.BUILD-SNAPSHOT.jar

printf "\n\nCompile\n"
native-image \
  -Dio.netty.noUnsafe=true \
  --no-server \
  -H:Name=logger \
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
  -cp $CP com.example.logger.LoggerApplication

mv logger ../../..

printf "\n\nJava exploded jar\n"
time java -classpath $CP com.example.logger.LoggerApplication

printf "\n\nCompiled app (logger)\n"
cd ../../..
time ./logger


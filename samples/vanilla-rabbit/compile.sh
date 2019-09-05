#!/usr/bin/env bash
../../mvnw -DskipTests clean package

export JAR="vanilla-rabbit-0.0.1.BUILD-SNAPSHOT.jar"
rm rabbit
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
#java -classpath $CP app.main.SampleApplication

# Our feature being on the classpath is what triggers it
export CP=$CP:../../../../../target/spring-graal-feature-0.6.0.BUILD-SNAPSHOT.jar

printf "\n\nCompile\n"
native-image \
  -Dio.netty.noUnsafe=true \
  --no-server \
  -H:Name=rabbit\
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
  -DremoveUnusedAutoconfig=true \
  -cp $CP app.main.SampleApplication

  #--debug-attach \
mv rabbit ../../..

printf "\n\nCompiled app (demo)\n"
cd ../../..
time ./rabbit


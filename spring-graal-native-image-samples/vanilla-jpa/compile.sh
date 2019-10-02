#!/usr/bin/env bash
export EXECUTABLE_NAME=jpa
export JAR="vanilla-jpa-0.0.1.BUILD-SNAPSHOT.jar"

../../mvnw -DskipTests clean package

rm $EXECUTABLE_NAME
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
  -H:Name=$EXECUTABLE_NAME \
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
  -DremoveUnusedAutoconfig=true \
  -cp $CP app.main.SampleApplication
  #--debug-attach \

mv $EXECUTABLE_NAME ../../..

printf "\n\nCompiled app...\n"
cd ../../..
#time ./orm -Dhibernate.dialect=org.hibernate.dialect.H2Dialect
# Do we need the -D on this one?
time ./$EXECUTABLE_NAME
# -Dhibernate.dialect=org.hibernate.dialect.H2Dialect


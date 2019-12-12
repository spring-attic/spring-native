#!/usr/bin/env bash
../../mvnw clean install

export JAR="webflux-netty-0.0.1-SNAPSHOT.jar"
rm webflux-netty
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
#java -classpath $CP com.example.demo.DemoApplication

# Our feature being on the classpath is what triggers it
export CP=$CP:../../../../../spring-graal-native-feature/target/spring-graal-native-feature-0.6.0.BUILD-SNAPSHOT.jar

printf "\n\nCompile\n"
native-image \
-Djava.awt.headless=true \
  --no-server \
  -Dverbose=true \
  -H:+TraceClassInitialization \
  -H:Name=webflux-netty \
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  -H:ClassInitialization=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration:run_time \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
  -DremoveUnusedAutoconfig=true \
  -cp $CP com.example.demo.DemoApplication

#
#--debug-attach \
mv webflux-netty ../../..

printf "\n\nCompiled app (webflux-netty)\n"
cd ../../..
time ./webflux-netty


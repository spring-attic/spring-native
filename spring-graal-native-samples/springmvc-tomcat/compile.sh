#!/usr/bin/env bash
../../mvnw -DskipTests clean package

export JAR="tomcat-0.0.1-SNAPSHOT.jar"
rm -f tc
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
  --no-server \
  --initialize-at-build-time=org.eclipse.jdt,org.apache.el.parser.SimpleNode,javax.servlet.jsp.JspFactory,org.apache.jasper.servlet.JasperInitializer,org.apache.jasper.runtime.JspFactoryImpl -H:+JNI \
  -H:EnableURLProtocols=http,https,jar \
  -H:ReflectionConfigurationFiles=../../../tomcat-reflection.json -H:ResourceConfigurationFiles=../../../tomcat-resource.json -H:JNIConfigurationFiles=../../../tomcat-jni.json \
  --enable-https \
  -H:+TraceClassInitialization \
  -H:IncludeResourceBundles=javax.servlet.http.LocalStrings \
  -H:Name=tc \
  -Djava.awt.headless=true \
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
  -Dsun.rmi.transport.tcp.maxConnectionThreads=0 \
  -DremoveUnusedAutoconfig=true \
  -cp $CP com.example.tomcat.TomcatApplication
  #-H:ClassInitialization=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration:run_time \

#native-image \
#  -Dio.netty.noUnsafe=true \
#  --enable-https \
#  --no-server \
#  -H:+TraceClassInitialization \
#  -H:IncludeResourceBundles=javax.servlet.http.LocalStrings \
#  -H:Name=tc \
#  -H:+ReportExceptionStackTraces \
#  --no-fallback \
#  --allow-incomplete-classpath \
#  --report-unsupported-elements-at-runtime \
#-Dsun.rmi.transport.tcp.maxConnectionThreads=0 \
#  -DremoveUnusedAutoconfig=true \
#  -cp $CP com.example.tomcat.TomcatApplication
#
mv tc ../../..

printf "\n\nCompiled app (tc)\n"
cd ../../..
time ./tc


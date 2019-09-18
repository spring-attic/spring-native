#!/usr/bin/env bash
#../../mvnw clean install

rm tc
cd unpack

cd BOOT-INF/classes
export LIBPATH=`find ../../BOOT-INF/lib | tr '\n' ':'`
export CP=.:$LIBPATH

export CP=$CP:../../../../../target/spring-graal-feature-0.6.0.BUILD-SNAPSHOT.jar

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
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
-Dsun.rmi.transport.tcp.maxConnectionThreads=0 \
  -DremoveUnusedAutoconfig=true \
  -cp $CP com.example.tomcat.TomcatApplication

mv tc ../../..


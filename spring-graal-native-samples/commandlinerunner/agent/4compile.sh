native-image \
  -Dspring.graal.mode=initialization-only \
  --no-server \
  --no-fallback \
  --allow-incomplete-classpath \
  -H:+TraceClassInitialization \
  -H:+ReportExceptionStackTraces \
  --report-unsupported-elements-at-runtime \
  -H:Name=commandlinerunner-agent \
  -cp .:$CP:graal:../../../../../../spring-graal-native/target/spring-graal-native-0.6.0.BUILD-SNAPSHOT.jar \
  com.example.commandlinerunner.CommandlinerunnerApplication 2>&1 | tee output.txt

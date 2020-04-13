native-image \
  -Dspring.graal.mode=initialization-only \
  --no-server \
  --no-fallback \
  -H:+TraceClassInitialization \
  -H:+ReportExceptionStackTraces \
  -H:Name=commandlinerunner-agent \
  -cp .:$CP:graal:../../../../../../spring-graal-native/target/spring-graal-native-0.7.0.BUILD-SNAPSHOT.jar \
  com.example.commandlinerunner.CommandlinerunnerApplication 2>&1 | tee output.txt

native-image \
  -Dmode=light \
  --no-server \
  --no-fallback \
  --allow-incomplete-classpath \
  -H:+TraceClassInitialization \
  -H:+ReportExceptionStackTraces \
  --report-unsupported-elements-at-runtime \
  -H:Name=commandlinerunner-agent \
  -cp .:$CP:graal:../../../../../../spring-graal-native-feature/target/spring-graal-native-feature-0.6.0.BUILD-SNAPSHOT.jar \
  com.example.commandlinerunner.CommandlinerunnerApplication 2>&1 | tee output.txt

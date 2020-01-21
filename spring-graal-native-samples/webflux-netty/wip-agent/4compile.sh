native-image \
  --no-server \
  -Djava.awt.headless=true \
  --no-fallback \
  --allow-incomplete-classpath \
  -H:+TraceClassInitialization \
  -H:+ReportExceptionStackTraces \
  --report-unsupported-elements-at-runtime \
  -H:Name=wn \
  -cp .:$CP:graal \
  com.example.demo.DemoApplication

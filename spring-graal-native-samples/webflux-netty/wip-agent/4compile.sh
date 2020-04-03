native-image \
  --no-server \
  -Djava.awt.headless=true \
  --no-fallback \
  -H:+TraceClassInitialization \
  -H:+ReportExceptionStackTraces \
  -H:Name=wn \
  -cp .:$CP:graal \
  com.example.demo.DemoApplication

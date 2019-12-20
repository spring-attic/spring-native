native-image \
  --no-server \
  -Djava.awt.headless=true \
  --no-fallback \
  --allow-incomplete-classpath \
  -H:IncludeResourceBundles=javax.servlet.http.LocalStrings \
  -H:IncludeResourceBundles=javax.servlet.LocalStrings \
  -H:+TraceClassInitialization \
  -H:+ReportExceptionStackTraces \
  --report-unsupported-elements-at-runtime \
  -H:Name=st \
  -cp .:$CP:graal \
  com.example.tomcat.TomcatApplication

#  -H:+PrintMethodHistogram \
#  -H:+PrintImageObjectTree \


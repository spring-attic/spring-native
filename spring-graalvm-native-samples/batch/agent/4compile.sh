native-image \
  -H:Name=batch-agent \
  -Dspring.spel.ignore=true \
  -cp .:$CP:graal \
  com.example.batch.BatchApplication 2>&1 | tee output.txt

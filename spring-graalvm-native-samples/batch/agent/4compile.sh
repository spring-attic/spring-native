native-image \
  -H:Name=batch-agent \
  -Dspring.spel.ignore=true \
  -Dspring.native.dump-config=/tmp/reflect-agent.json \
  -cp .:$CP:graal \
  com.example.batch.BatchApplication 2>&1 | tee output.txt

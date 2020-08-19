native-image \
  -H:Name=commandlinerunner-agent \
  -Dspring.spel.ignore=true \
  -cp .:$CP:graal \
  com.example.commandlinerunner.CommandlinerunnerApplication 2>&1 | tee output.txt

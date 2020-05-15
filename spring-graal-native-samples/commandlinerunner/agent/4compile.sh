native-image \
  -Dspring.graal.mode=agent \
  --no-server \
  --no-fallback \
  -H:Name=commandlinerunner-agent \
  -Dspring.graal.remove-xml-support=true \
  -Dspring.graal.remove-spel-support=true \
  -Dspring.graal.remove-jmx-support=true \
  -cp .:$CP:graal \
  com.example.commandlinerunner.CommandlinerunnerApplication 2>&1 | tee output.txt

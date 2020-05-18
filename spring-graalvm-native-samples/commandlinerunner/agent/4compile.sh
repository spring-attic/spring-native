native-image \
  -Dspring.native.mode=agent \
  --no-server \
  --no-fallback \
  -H:Name=commandlinerunner-agent \
  -Dspring.native.remove-xml-support=true \
  -Dspring.native.remove-spel-support=true \
  -Dspring.native.remove-jmx-support=true \
  -cp .:$CP:graal \
  com.example.commandlinerunner.CommandlinerunnerApplication 2>&1 | tee output.txt

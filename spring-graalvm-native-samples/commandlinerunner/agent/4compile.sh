native-image \
  -H:Name=commandlinerunner-agent \
  -Dspring.native.mode=agent \
  -Dspring.xml.ignore=true \
  -Dspring.spel.ignore=true \
  -Dspring.native.remove-jmx-support=true \
  -cp .:$CP:graal \
  com.example.commandlinerunner.CommandlinerunnerApplication 2>&1 | tee output.txt

java -cp .:$CP \
  -agentlib:native-image-agent=config-output-dir=graal/META-INF/native-image \
  com.example.commandlinerunner.CommandlinerunnerApplication

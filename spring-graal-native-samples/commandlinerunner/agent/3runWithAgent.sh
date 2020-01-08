#!/usr/bin/env bash
echo "Running with agent for 3 seconds"
java -cp .:$CP \
  -agentlib:native-image-agent=config-output-dir=graal/META-INF/native-image \
  com.example.commandlinerunner.CommandlinerunnerApplication > agent-output.txt 2>&1 &
PID=$!
sleep 3

kill ${PID}
sleep 3

#!/usr/bin/env bash

# Build the project and unpack the fat jar ready to run
./1prepare.sh

# Set the CP for the jars/code in the unpacked application, leaving us in the BOOT-INF/classes folder
. ./2setcp.sh

# Run the application with the agent to populate the configuration files
../../../3runWithAgent.sh

# Run native image to compile the application
../../../4compile.sh

# Test the application

# The test script will look for it in the current folder
cp ../../../verify.sh .
${PWD%/*samples/*}/scripts/test.sh commandlinerunner-agent .
mkdir -p ../../../target/native-image/
mv summary.csv ../../../target/native-image/

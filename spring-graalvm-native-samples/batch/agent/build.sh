#!/usr/bin/env bash

# Build the project and unpack the fat jar ready to run
./1prepare.sh

echo "1 is done"
pwd

# Set the CP for the jars/code in the unpacked application, leaving us in the BOOT-INF/classes folder
. ./2setcp.sh
echo "2 is done"
pwd

# Run the application with the agent to populate the configuration files
../../../3runWithAgent.sh
echo "3 is done"
pwd

# Run native image to compile the application
../../../4compile.sh
echo "4 is done"
pwd

# Test the application

# The test script will look for it in the current folder
cp ../../../verify.sh .
${PWD%/*samples/*}/scripts/test.sh batch-agent .
mkdir -p ../../../target/native-image/
mv summary.csv ../../../target/native-image/

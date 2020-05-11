#!/usr/bin/env bash

export MAVEN_OPTS=-B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

mvn clean install

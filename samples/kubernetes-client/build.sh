#!/usr/bin/env bash

mvn io.spring.javaformat:spring-javaformat-maven-plugin:0.0.27:apply
mvn -DskipTests=true clean package spring-boot:build-image

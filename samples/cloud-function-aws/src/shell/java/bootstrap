#!/bin/sh

cd ${LAMBDA_TASK_ROOT:-.}

java -Dspring.main.web-application-type=none -Dlogging.level.org.springframework=DEBUG \
  -noverify -XX:TieredStopAtLevel=1 -Xss256K -XX:MaxMetaspaceSize=128M \
  -cp .:`echo lib/*.jar | tr ' ' :` com.example.demo.DemoApplication
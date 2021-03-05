#!/usr/bin/env bash
RESPONSE=`curl -H "Accept: application/vnd.spring-cloud.config-server.v2+json" localhost:9000/config-client/dev/master | jq -r '.propertySources[0].source."my.prop".value'`
if [[ $RESPONSE != "from application-dev.yml" ]]; then
  exit 1
fi
RESPONSE=`curl localhost:9000/config-client/dev/master | jq -r '.propertySources[0].source."my.prop"'`
if [[ $RESPONSE != "from application-dev.yml" ]]; then
  exit 2
fi

exit 0
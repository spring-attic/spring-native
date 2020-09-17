#!/usr/bin/env bash
RESPONSE=`curl -s localhost:8080/actuator/health`
if [[ "$RESPONSE" == *"UP"* ]]; then
R_BEANS=`curl -s localhost:8080/actuator/beans | jq ".contexts.application.beans[] | .type" | wc -l`
if (( R_BEANS > 200 )); then
R_CONFIGPROPS=`curl -s localhost:8080/actuator/configprops | jq ".contexts.application.beans[] | .properties"  | wc -l`
if (( R_CONFIGPROPS > -1 )); then
R_CONDITIONS=`curl -s localhost:8080/actuator/conditions | jq ".contexts.application.positiveMatches | keys" | wc -l`
if (( R_CONDITIONS > 100 )); then
R_ENV=`curl -s localhost:8080/actuator/env | jq ".propertySources | map(.name)" | wc -l`
if (( R_ENV > 5 )); then
  exit 0
else
  echo "did not get at least 5 property sources $R_ENV"
fi
else
  echo "did not get at least 100 postitive condition matches $R_CONDITIONS"
fi
else 
  echo "did not get at least 200 properties objects $R_CONFIGPROPS"
fi
else
  echo "did not get at least 200 beans $R_BEANS"
fi
else 
  echo "failed to get UP health response"
fi
exit 1

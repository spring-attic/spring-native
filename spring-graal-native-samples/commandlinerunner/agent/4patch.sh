#!/usr/bin/env bash
echo "Adding in missing reflect-config.json entries"
sed -e "/^\[/r ../../../missing-reflect-config.json" graal/META-INF/native-image/reflect-config.json > graal/META-INF/native-image/reflect-config.json.new
mv graal/META-INF/native-image/reflect-config.json.new graal/META-INF/native-image/reflect-config.json

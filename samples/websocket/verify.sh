#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_log target/native/test-output.txt "Started WebsocketApplication"
wait_log target/native/test-output.txt "Client: Sent 'Hello Websocket'"
wait_log target/native/test-output.txt "Server: Received 'Hello Websocket'"
wait_log target/native/test-output.txt "Client: Received 'Hello Websocket'"
wait_log target/native/test-output.txt "Server: Sent 'Hello Websocket'"

#!/usr/bin/env bash
source ${PWD%/*samples/*}/scripts/wait.sh
wait_log target/native/test-output.txt "Started WebsocketStompApplication"
wait_log target/native/test-output.txt "STOMP Client connected"
wait_log target/native/test-output.txt "Server: Received HelloMessage{name='STOMP Client'}"
wait_log target/native/test-output.txt "Client: Received 'GreetingMessage{content='Hello, STOMP Client!'}'"

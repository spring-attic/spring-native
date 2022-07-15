# Wait a term appears in logs (30 seconds as default timeout)
# Usage: wait_log file search_term [timeout]
wait_log() {
  local file="$1"; shift
  local search_term="$1"; shift
  local timeout="${1:-30}"; shift # 30 seconds as default timeout

  for ((i=0; i<=timeout; i++)); do
    if test -f "$file"; then
      cat "$file" | grep -Eq "$search_term" && return 0
    fi
    sleep 1
  done

  echo "Timeout of $timeout seconds reached. Unable to find '$search_term' in '$file'"
  return 1
}

# Wait a term appears in an HTTP endpoint (30 seconds as default timeout)
# Usage: wait_http url search_term [timeout]
wait_http() {
  local url="$1"; shift
  local search_term="$1"; shift
  local timeout="${1:-30}"; shift # 30 seconds as default timeout

  for ((i=0; i<=timeout; i++)); do
    RESPONSE=`curl --max-time 2 -s $url`
    if [[ "$RESPONSE" == *"$search_term"* ]]; then
      return 0
    fi
    sleep 1
  done

  echo "Timeout of $timeout seconds reached. Unable to find '$search_term' in '$url'"
  return 1
}

# Wait a term appears in an HTTP POST endpoint (30 seconds as default timeout)
# Usage: wait_http url json_string search_term [timeout]
wait_http_post() {
  local url="$1"; shift
  local json_string="$1"; shift
  local search_term="$1"; shift
  local timeout="${1:-30}"; shift # 30 seconds as default timeout

  for ((i=0; i<=timeout; i++)); do
    RESPONSE=`curl -X POST -H 'Content-Type: application/json' -d "$json_string" --max-time 2 -s $url`
    if [[ "$RESPONSE" == *"$search_term"* ]]; then
      return 0
    fi
    sleep 1
  done

  echo "Timeout of $timeout seconds reached. Unable to find '$search_term' in POST '$url'"
  return 1
}

# Wait a term is returned by the specified command (30 seconds as default timeout)
# Usage: wait_command_output command search_term [timeout]
wait_command_output() {
  local command="$1"; shift
  local search_term="$1"; shift
  local timeout="${1:-30}"; shift # 30 seconds as default timeout

  for ((i=0; i<=timeout; i++)); do
    if [[ `eval $command` == *"$search_term"* ]]; then
      return 0
    fi
    sleep 1
  done

  echo "Timeout of $timeout seconds reached. Unable to find '$search_term' in '$url'"
  return 1
}

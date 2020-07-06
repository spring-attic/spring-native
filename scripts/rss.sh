PID=$1
RSS=`ps -o rss ${PID} | tail -n1`
RSS=`bc <<< "scale=1; ${RSS}/1024"`
echo "RSS memory (PID: ${PID}): ${RSS}M"

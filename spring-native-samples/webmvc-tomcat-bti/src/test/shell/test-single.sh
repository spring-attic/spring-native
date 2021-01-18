#!/bin/bash
echo "### utilisation test start"
echo $@

mkdir -p target/plots
mkdir -p target/logs

time_start=$(date +%s%N)

$1 &
MY_PID=$!

sleep 0.02
psrecord $MY_PID --plot "target/plots/$3.png" --log "target/logs/$3.log" --interval 0.2 & PSRECORD_PID=$!

# sleep until ready
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' $2)" != "200" ]]; do sleep 0.02; done

time_end=$(date +%s%N)
time_spent=$((($time_end - $time_start)/1000000))
echo "### server ready after $time_spent ms"
echo "$MY_PID:$time_spent" > target/plots/time-server-ready.txt

sleep 1 

# start load-test using apache benchmarking tool
time_start=$(date +%s%N)
ab -c 5 -n 5000 $2
time_end=$(date +%s%N)
time_spent=$((($time_end - $time_start)/1000000))
echo "### load-test $time_spent ms"
echo "$MY_PID:$time_spent" > target/plots/time-load-test.txt

sleep 1
kill -9 $MY_PID
echo "### utilisation test finished"
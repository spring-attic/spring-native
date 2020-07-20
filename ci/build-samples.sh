#!/usr/bin/env sh

mkdir /tmp/data
/usr/bin/mongod --fork --dbpath /tmp/data --logpath /tmp/data/mongod.log
redis-server --daemonize yes
chown -R mysql:mysql /var/lib/mysql
chown -R mysql:mysql /var/log/mysql
chown -R mysql:mysql /var/run/mysqld
/tmp/mysql.sh
cd spring-graalvm-native
./build.sh
./build-samples.sh
killall /usr/sbin/mysqld

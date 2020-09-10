#!/usr/bin/env sh

RC=0
mkdir /tmp/data
/usr/bin/mongod --fork --dbpath /tmp/data --logpath /tmp/data/mongod.log
redis-server --daemonize yes
elasticsearch start
chown -R mysql:mysql /var/lib/mysql
chown -R mysql:mysql /var/log/mysql
chown -R mysql:mysql /var/run/mysqld
/tmp/mysql.sh
native-image --version
cd spring-graalvm-native
if ! (./build.sh); then
    RC=1
fi
if ! (./build-samples.sh); then
    RC=1
fi
mysqladmin shutdown
exit $RC
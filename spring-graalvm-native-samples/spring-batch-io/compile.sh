#!/usr/bin/env bash

ARTIFACT=spring-batch-io
MAINCLASS=com.example.batch.BatchConfiguration
VERSION=0.0.1-SNAPSHOT

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

rm -rf target
mkdir -p target/native-image

echo "Packaging $ARTIFACT with Maven"
mvn -ntp package > target/native-image/output.txt

JAR="$ARTIFACT-$VERSION.jar"
rm -f $ARTIFACT
echo "Unpacking $JAR"
cd target/native-image
jar -xvf ../$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

LIBPATH=`find BOOT-INF/lib | tr '\n' ':'`
CP=BOOT-INF/classes:$LIBPATH

GRAALVM_VERSION=`native-image --version`
echo "Compiling $ARTIFACT with $GRAALVM_VERSION"
time native-image \
  --verbose \
  -H:Name=$ARTIFACT \
  -Dspring.native.remove-unused-autoconfig=true \
  --initialize-at-build-time=org.hsqldb.jdbc.JDBCDriver \
  --initialize-at-build-time=org.springframework.batch.core.JobParameters \
  --initialize-at-build-time=org.springframework.batch.core.JobInstance \
  --initialize-at-build-time=org.springframework.beans.factory.InitializingBean \
  --initialize-at-build-time=org.springframework.batch.core.launch.JobOperator \
  --initialize-at-build-time=org.springframework.batch.core.Job \
  --initialize-at-build-time=org.springframework.batch.core.configuration.JobFactory \
  --initialize-at-build-time=org.springframework.batch.core.Entity \
  --initialize-at-build-time=org.springframework.batch.core.configuration.JobRegistry \
  --initialize-at-build-time=org.springframework.batch.core.JobExecution \
  --initialize-at-build-time=org.springframework.batch.core.StepExecution \
  --initialize-at-build-time=org.springframework.batch.repository.JobRepository \
  --initialize-at-build-time=org.springframework.batch.launch.JobLauncher \
  --initialize-at-build-time=org.springframework.transaction.TransactionStatus \
  --initialize-at-build-time=org.springframework.transaction.TransactionDefinition \
  --initialize-at-build-time=org.springframework.transaction.PlatformTransactionManager \
  --initialize-at-build-time=org.springframework.batch.core.repository.JobRepository \
  --initialize-at-build-time=org.springframework.batch.core.launch.JobLauncher \
  --initialize-at-build-time=org.springframework.batch.item.ItemStreamReader \
  --initialize-at-build-time=org.springframework.batch.item.file.ResourceAwareItemWriterItemStream \
  --initialize-at-build-time=org.springframework.aop.scope.ScopedObject \
  --initialize-at-build-time=org.springframework.batch.item.ExecutionContext \
  --initialize-at-build-time=org.springframework.core.io.Resource \
  -cp $CP:../../graal $MAINCLASS 2>&1 | tee output.txt

if [[ -f $ARTIFACT ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
  mv ./$ARTIFACT ..
  exit 0
else
  cat output.txt
  printf "${RED}FAILURE${NC}: an error occurred when compiling the native-image.\n"
  exit 1
fi

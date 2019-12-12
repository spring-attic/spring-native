#!/usr/bin/env bash
../../mvnw -DskipTests clean package

export JAR="spring-petclinic-jpa-2.1.0.BUILD-SNAPSHOT.jar"
rm -f petclinic
printf "Unpacking $JAR"
rm -rf unpack
mkdir unpack
cd unpack
jar -xvf ../target/$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

cd BOOT-INF/classes
export LIBPATH=`find ../../BOOT-INF/lib | tr '\n' ':'`
export CP=.:$LIBPATH
#:/Users/aclement/gits/spring-graal-native/spring-graal-native-samples/spring-petclinic-jpa/graal

# This would run it here... (as an exploded jar)
#java -classpath $CP com.example.demo.DemoApplication

# Our feature being on the classpath is what triggers it
export CP=$CP:../../../../../spring-graal-native-feature/target/spring-graal-native-feature-0.6.0.BUILD-SNAPSHOT.jar

printf "\n\nCompile\n"

#native-image -Dio.netty.noUnsafe=true --no-server -H:+TraceClassInitialization \
#  -H:Name=petclinic -H:+ReportExceptionStackTraces --no-fallback \
#  --allow-incomplete-classpath --report-unsupported-elements-at-runtime -DremoveUnusedAutoconfig=true \
#  -cp $CP org.springframework.samples.petclinic.PetClinicApplication

#-H:+PrintUniverse \
native-image -DavoidLogback=true -Ddebug=true -Dio.netty.noUnsafe=true --no-server \
  -H:Name=petclinic -H:+ReportExceptionStackTraces -H:+TraceClassInitialization \
-H:IncludeResourceBundles=messages/messages \
  --no-fallback --allow-incomplete-classpath --report-unsupported-elements-at-runtime -cp $CP \
  --initialize-at-build-time=org.springframework.boot.validation.MessageInterpolatorFactory,org.hsqldb.jdbc.JDBCDriver,com.mysql.cj.jdbc.Driver,org.springframework.samples.petclinic.owner.PetRepository,org.springframework.samples.petclinic.owner.OwnerRepository,org.springframework.samples.petclinic.visit.VisitRepository,org.springframework.samples.petclinic.vet.VetRepository,org.springframework.samples.petclinic.owner.Pet,org.springframework.samples.petclinic.owner.Owner,org.springframework.samples.petclinic.model.NamedEntity,org.springframework.samples.petclinic.model.Person,org.springframework.samples.petclinic.model.BaseEntity,org.springframework.samples.petclinic.model.NamedEntity,org.springframework.samples.petclinic.visit.Visit,org.springframework.samples.petclinic.vet.Vet \
  org.springframework.samples.petclinic.PetClinicApplication


mv petclinic ../../..

printf "\n\nCompiled app (petclinic)\n"
cd ../../..
time ./petclinic


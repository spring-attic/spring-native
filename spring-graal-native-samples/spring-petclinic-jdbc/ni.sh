#!/usr/bin/env bash
cd unpack/BOOT-INF/classes
export LIBPATH=`find ../../BOOT-INF/lib | tr '\n' ':'`
export CP=.:$LIBPATH

# Our feature being on the classpath is what triggers it
export CP=$CP:../../../../../spring-graal-native/target/spring-graal-native-0.7.0.BUILD-SNAPSHOT.jar

printf "\n\nCompile\n"

#-H:+PrintUniverse \
native-image \
  -Ddebug=true \
  -Dio.netty.noUnsafe=true \
  --no-server \
  -H:Name=petclinic \
  -H:+ReportExceptionStackTraces \
  -H:+TraceClassInitialization \
  -H:IncludeResourceBundles=messages/messages \
  --no-fallback \
  -cp $CP \
  --initialize-at-build-time=org.springframework.boot.validation.MessageInterpolatorFactory,org.hsqldb.jdbc.JDBCDriver,com.mysql.cj.jdbc.Driver,org.springframework.samples.petclinic.owner.PetRepository,org.springframework.samples.petclinic.owner.OwnerRepository,org.springframework.samples.petclinic.visit.VisitRepository,org.springframework.samples.petclinic.vet.VetRepository,org.springframework.samples.petclinic.owner.Pet,org.springframework.samples.petclinic.owner.Owner,org.springframework.samples.petclinic.model.NamedEntity,org.springframework.samples.petclinic.model.Person,org.springframework.samples.petclinic.model.BaseEntity,org.springframework.samples.petclinic.model.NamedEntity,org.springframework.samples.petclinic.visit.Visit,org.springframework.samples.petclinic.vet.Vet \
  org.springframework.samples.petclinic.PetClinicApplication

mv petclinic ../../..

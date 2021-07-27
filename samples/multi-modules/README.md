# Multi-Modules Buildpacks sample

The goal of this sample is to document how applications can leverage the Spring Boot and AOT plugins in a multi-module setup with Buildpacks.

This project contains two modules:

* the "core" module is a library used by our application.
  In general, such modules should hold dependencies to other libraries, but not Spring Boot starters.
* the "app" module contains the main application class and creates the native image. 

You can try this sample using Gradle:

```shell
# build the native image with Buildpacks
$ ./gradlew :app:bootBuildImage
```

Or Maven:

```shell
# build the native image with Buildpacks
$ mvn package -PbuildImage
```
Spring Boot project with Spring Data MongoDB.

To build and run the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker-compose up
```

As an alternative, you can use `build.sh` (with a local GraalVM installation or combined with
`run-dev-container.sh` at the root of `spring-graalvm-native` project). See also the related issue
[https://github.com/spring-projects-experimental/spring-graalvm-native/issues/227](Take advantage of Paketo dev-oriented images).
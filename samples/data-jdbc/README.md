Spring Boot project with Spring Data JDBC.

To build and run the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
```

As an alternative, you can use `build.sh` (with a local GraalVM installation or combined with
`run-dev-container.sh` at the root of `spring-native` project). See also the related issue
[Take advantage of Paketo dev-oriented images](https://github.com/spring-projects-experimental/spring-native/issues/227).

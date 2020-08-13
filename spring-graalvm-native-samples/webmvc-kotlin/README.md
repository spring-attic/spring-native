Spring Boot project with Spring MVC, Tomcat, Jackson and Kotlin.

To build and run the native application packaged in a lightweight container:
```
./gradlew bootBuildImage
docker run -p 8080:8080 docker.io/library/webmvc-kotlin:0.0.1-SNAPSHOT
```

And then go to [http://localhost:8080/](http://localhost:8080/).

As an alternative, you can use `build.sh` (with a local GraalVM installation or combined with
`run-dev-container.sh` at the root of `spring-graalvm-native` project). See also the related issue
[https://github.com/spring-projects-experimental/spring-graalvm-native/issues/227](Take advantage of Paketo dev-oriented images).
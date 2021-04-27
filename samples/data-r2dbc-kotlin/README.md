Spring Boot project with Spring Webflux, Netty and R2DBC written in Kotlin.

To build and run the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker-compose up
```

And then go to [http://localhost:8080/reservations](http://localhost:8080/reservations).

As an alternative, you can use `build.sh` (with a local GraalVM installation or combined with
`run-dev-container.sh` at the root of `spring-native` project). See also the related issue
[Take advantage of Paketo dev-oriented images](https://github.com/spring-projects-experimental/spring-native/issues/227).

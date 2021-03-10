Very basic Spring Boot project that uses Spring Cloud Task.

To build and run the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker-compose up
```

As an alternative, you can use `build.sh` (with a local GraalVM installation).
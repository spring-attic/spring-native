Spring Boot project with Spring Webflux client, Netty and Jackson.

To build and run the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker run docker.io/library/webclient:0.0.1-SNAPSHOT
```

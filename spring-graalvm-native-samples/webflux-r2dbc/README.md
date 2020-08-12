Spring Boot project with Spring Webflux, Netty and R2DBC.

To build and run the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker run docker.io/library/webflux-r2dbc:0.0.1-SNAPSHOT
```

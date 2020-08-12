This sample leverages [Jafu DSL](https://github.com/spring-projects-experimental/spring-fu/tree/master/jafu) from [Spring Fu](https://github.com/spring-projects-experimental/spring-fu) in order to allow smaller image size and lower memory consumption.

To build and run the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker run docker.io/library/webmvc-tomcat-jafu:0.0.1-SNAPSHOT
```
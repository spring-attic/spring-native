Spring Boot project with Spring MVC, Tomcat and Jackson.

To build and run the native application packaged in a lightweight container with `default` mode:
```
mvn spring-boot:build-image
docker run docker.io/library/webmvc-tomcat:0.0.1-SNAPSHOT
```

To do it with `hybrid` mode:
```
mvn -P hybrid spring-boot:build-image
docker run docker.io/library/webmvc-tomcat:0.0.1-SNAPSHOT
```
Spring Boot project with Spring MVC, Tomcat, Jackson and Kotlin.

To build and run the native application packaged in a lightweight container:
```
./gradlew bootBuildImage
docker run docker.io/library/webmvc-tomcat-kotlin:0.0.1-SNAPSHOT
```

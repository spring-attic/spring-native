Very basic spring boot project using a CommandLineRunner bean.

To build the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker run docker.io/library/commandlinerunner:0.0.1-SNAPSHOT
```

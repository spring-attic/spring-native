This is a variant of the commandlinerunner sample that uses Maven and GraalVM tracing agent to drive the native-image construction.

To build the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker run docker.io/library/commandlinerunner-agent:0.0.1-SNAPSHOT
```
This project is exercising Spring Features from the application point of view (the other
samples typically exercise them via picking up dependencies containing them).

To build and run the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker-compose up
```

As an alternative, you can use `build.sh` (with a local GraalVM installation or combined with
`run-dev-container.sh` at the root of `spring-native` project). See also the related issue
[Take advantage of Paketo dev-oriented images](https://github.com/spring-projects-experimental/spring-native/issues/227).
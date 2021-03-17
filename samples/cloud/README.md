Spring Boot project with Spring Webflux and Spring Cloud. The config client is working (you will see it reach out to localhost:8888 for some configuration data on startup). The /features endpoint is also working, but some other things are not compatible with a regular JVM (for instance there seems to be an issue with discovery client being present but not triggering a load balancer to be created).

To build and run the native application packaged in a lightweight container with `default` mode:
```
mvn spring-boot:build-image
docker-compose up
```

And then go to [http://localhost:8080/](http://localhost:8080/).

As an alternative, you can use `build.sh` (with a local GraalVM installation or combined with
`run-dev-container.sh` at the root of `spring-native` project).
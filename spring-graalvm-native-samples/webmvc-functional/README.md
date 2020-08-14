This sample runs with no annotation processing and no .class files shipped for condition processing.
Hence it runs as a native image with `spring.native.mode=functional`.

With Java 8: the image size=35M, RSS=36M (excluding Jackson as well to squeeze out at extra 1MB). Compare with `spring.native.mode=default`: size=54M, RSS=58M.

To build and run the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker-compose up
```

And then go to [http://localhost:8080/](http://localhost:8080/).
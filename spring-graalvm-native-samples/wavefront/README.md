Spring Boot project with Wavefront and Sleuth.

To build and run the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker run -p 8080:8080 -v ${HOME}:/home/cnb docker.io/library/wavefront:0.0.1-SNAPSHOT
```

And then go to [http://localhost:8080/](http://localhost:8080/).
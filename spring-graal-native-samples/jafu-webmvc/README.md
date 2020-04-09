This sample leverages Jafu DSL from [Spring Fu incubator](https://github.com/spring-projects-experimental/spring-fu) in order to allow smaller image size and lower memory consumption.

Notice there is currently a regression in GraalVM on XML related code removal capabilities, see figures bellow and related issue https://github.com/oracle/graal/issues/2327.

With GraalVM 19.3.1:

- Image build time: 78.8s
- RSS memory: 38.3M
- Image size: 37.8M

With GraalVM 20.0.0:

- Image build time: 87.6s
- RSS memory: 50.6M
- Image size: 55.8M

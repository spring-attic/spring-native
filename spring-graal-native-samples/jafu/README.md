This sample leverages Jafu DSL from [Spring Fu incubator](https://github.com/spring-projects-experimental/spring-fu) in order to allow smaller image size and lower memory consumption.

Notice there is currently a regression in GraalVM on XML related code removal capabilities, see figures bellow and related issue https://github.com/oracle/graal/issues/2327.

With GraalVM 19.3.1:

- Image build time: 36.2s
- RSS memory: 14.7M
- Image size: 15.0M

With GraalVM 20.0.0:

- Image build time: 43.2s
- RSS memory: 17.9M
- Image size: 21.8M

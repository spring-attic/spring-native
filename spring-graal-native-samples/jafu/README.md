This sample reuses some code from [Spring Fu incubator](https://github.com/spring-projects-experimental/spring-fu)
and leverages an optimized builtin GraalVM feature that discards CGLIB and
`ImageBanner`. Class initialization is configured by default at
build time.

This Spring Boot application has following characteristics:

 * Compile in 55s on my laptop
 * Image size: 17M
 * RSS memory: 13.4M
 * Started application in 0.003 seconds (JVM running for 0.003)
[![Build Status](https://ci.spring.io/api/v1/teams/spring-graal-native/pipelines/spring-graal-native/badge)](https://ci.spring.io/teams/spring-graal-native/pipelines/spring-graal-native)

This project goal is to provide experimental support for building [Spring Boot](https://spring.io/projects/spring-boot) applications as [GraalVM native-images](https://www.graalvm.org/docs/reference-manual/native-image/).
See this [Running Spring Boot applications as GraalVM native images](https://www.youtube.com/watch?v=3eoAxphAUIg) Devoxx talk video for more details.

It is mainly composed of 5 parts:

- `spring-graal-native-feature`: this module is Spring GraalVM feature. A feature here is a GraalVM term meaning a plugin for the native-image compilation process (which creates the native-image from the built class files). The feature participates in the compilation lifecycle, being invoked at different compilation stages to offer extra information about the application to aid in the image construction.
- `spring-graal-native-configuration`: this module contains configuration hints for Spring classes, including various Spring Boot auto-configurations.
- `spring-graal-native-substitutions`: this module allows to patch temporarily some part of Spring Boot and Spring Framework to improve compatibility and efficiency of Spring native images.
- `spring-graal-native`: this module aggregates the feature, configuration and substitutions ones to generate the artifact to consume.
- `spring-graal-native-samples`: contains various samples that demonstrate the feature usage and are used as integration tests.

For more detailed information on the feature and how to use it, please jump over to the [wiki](https://github.com/spring-projects-experimental/spring-graal-native/wiki). 

## Scope and status

This project status is alpha, that means that we are currently mainly working on the software design and on supporting the features of the current samples.
Supporting a wider and clearly defined range of Spring Boot applications, as well as optimizing image size and memory consumption, will happen as a second step.   

This feature supports:

- GraalVM 20.0.0
- Spring Boot 2.3.0.M4

## Quick start

For detailed information and walkthroughs of applying the techniques to your project, please see the [wiki](https://github.com/spring-projects-experimental/spring-graal-native/wiki).

### Install GraalVM native

From GraalVM builds:

- Install GraalVM from [here](https://github.com/graalvm/graalvm-ce-builds/releases).
- Set `JAVA_HOME` and `PATH` appropriately for that GraalVM version.
- Run `gu install native-image` to bring in the native-image extensions to the JDK.

Or you can use [SDKMAN](https://sdkman.io/) to easily switch between GraalVM versions:

- [Install SDKMAN](https://sdkman.io/install)
- Install GraalVM with `sdk install java 20.0.0.r8-grl` for Java 8 or `sdk install java 20.0.0.r11-grl` for Java 11
- Run `gu install native-image` to bring in the native-image extensions to the JDK.

### Artifacts

- Artifact: [`org.springframework.experimental:spring-graal-native:0.6.0.BUILD-SNAPSHOT`](https://repo.spring.io/snapshot/org/springframework/experimental/spring-graal-native/0.6.0.BUILD-SNAPSHOT/spring-graal-native-0.6.0.BUILD-SNAPSHOT.jar).

- Repositories: `https://repo.spring.io/milestone` for 0.x milestones or `https://repo.spring.io/snapshot` for snapshots.
 
### Play with the samples

- `git clone https://github.com/spring-projects-experimental/spring-graal-native`
- In the project root, run `./build.sh` 
- Go into the samples folder and pick one (e.g. `cd spring-graal-native-samples/commandlinerunner`)
- Run `./build.sh` which will run a maven build, then a native image compile, then test the result.

`build.sh` runs the `compile.sh` script and in that compile script you can see the invocation of the `native-image` command. The other samples follow a similar model. For more details on the samples see the [Samples wiki page](https://github.com/spring-projects-experimental/spring-graal-native/wiki/Samples).

## Contributing

This project is in the spring-projects-experimental org indicating it is not as mature as other Spring projects. Contributions are welcome (maybe read the [extension guide](https://github.com/spring-projects-experimental/spring-graal-native/wiki/ExtensionGuide) if thinking about extending it to support your project). However, please recognize we aren't at the polishing javadoc stage and whilst pre 1.0 there may be heavy evolution of APIs.


## License

[Apache License v2.0](https://www.apache.org/licenses/LICENSE-2.0)

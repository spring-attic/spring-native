[![Build Status](https://ci.spring.io/api/v1/teams/spring-graal-native/pipelines/spring-graal-native/badge)](https://ci.spring.io/teams/spring-graal-native/pipelines/spring-graal-native)

# spring-graal-native

This project contains code to enable the building of native images with GraalVM.
This repository contains a feature and a number of samples that demonstrate the feature usage. A feature here is a GraalVM term meaning a plugin for the native-image compilation process (which creates the native-image from the built class files). The feature participates in the compilation lifecycle, being invoked at different compilation stages to offer extra information about the application to aid in the image construction.

Once built, native images have very fast startup!

This feature supports:

- GraalVM 20.0
- Spring Boot 2.3.0.M3 (you may be able to get some things working with Boot 2.2.X but not 2.1 or earlier)

For more detailed information on the feature and how to use it, please jump over to the [wiki](https://github.com/spring-projects-experimental/spring-graal-native/wiki). There is a [video](https://www.youtube.com/watch?v=OxS66Q26ykA) of a presentation covering this project from Spring One Platform 2019.


## Quick start

For detailed information and walkthroughs of applying the techniques to your project, please see the [wiki](https://github.com/spring-projects-experimental/spring-graal-native/wiki).

### Install GraalVM native

From GraalVM builds:

- Install Graal 20.0 from [here](https://github.com/graalvm/graalvm-ce-builds/releases).
- Set `JAVA_HOME` and `PATH` appropriately for that Graal version.
- Run `gu install native-image` to bring in the native-image extensions to the JDK.

Or you can use [SDKMAN](https://sdkman.io/) to easily switch between GraalVM versions:

- [Install SDKMAN](https://sdkman.io/install)
- Install GraalVM with `sdk install java 20.0.0.r8-grl` for Java 8 or `sdk install java 20.0.0.r11-grl` for Java 11
- Run `gu install native-image` to bring in the native-image extensions to the JDK.

### Artifacts

The repositories to use are `https://repo.spring.io/milestone` for 0.x milestones or `https://repo.spring.io/snapshot/` for snapshots.

The artifact available is `org.springframework.experimental:spring-graal-native:0.6.0.BUILD-SNAPSHOT`.
 
### Play with the samples

- `git clone https://github.com/spring-projects-experimental/spring-graal-native`
- In the project root, run `./build-feature.sh` 
- Go into the samples folder and pick one (e.g. `cd spring-graal-native-samples/commandlinerunner`)
- Run `./build.sh` which will run a maven build, then a native image compile, then test the result.

`build.sh` runs the `compile.sh` script and in that compile script you can see the invocation of the `native-image` command. The other samples follow a similar model. For more details on the samples see the [Samples wiki page](https://github.com/spring-projects-experimental/spring-graal-native/wiki/Samples).

## FAQ

*Q. I get out of memory problems?*

A. native-image likes to use a _lot_ of RAM. There have been problems observed at 8G.

*Q. This just isn't working for my project! Why not?*

A. The samples demonstrate the kinds of Spring feature that have been tested. If having problems it is *probably* just that extra configuration is required rather than fundamentally impossible to make it work (however, this can depend what other libraries are in the project dependencies). Consult the [Troubleshooting](https://github.com/spring-projects-experimental/spring-graal-native/wiki/Troubleshooting) page for more details on dealing with specific problems. There are some features projects might be using that are not supported by GraalVM native-images, they are documented [here](https://github.com/oracle/graal/blob/master/substratevm/LIMITATIONS.md).

*Q. What's the roadmap?*

A. The feature is evolving and features are coming through in base Spring all the time too. Things should only improve in terms of compatibility across Spring and also smaller images, even faster startups and lower memory profiles.


## Contributing

This project is in the spring-projects-experimental org indicating it is not as mature as other Spring projects. Contributions are welcome (maybe read the [extension guide](https://github.com/spring-projects-experimental/spring-graal-native/wiki/ExtensionGuide) if thinking about extending it to support your project). However, please recognize we aren't at the polishing javadoc stage and whilst pre 1.0 there may be heavy evolution of APIs.


## License

[Apache License v2.0](https://www.apache.org/licenses/LICENSE-2.0)


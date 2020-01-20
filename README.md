# spring-graal-native

This project contains code to enable the building of native images with GraalVM.
This repository contains a feature and a number of samples that demonstrate the feature usage. A feature here is a GraalVM term meaning a plugin for the native-image compilation process (which creates the native-image from the built class files). The feature participates in the compilation lifecycle, being invoked at different compilation stages to offer extra information about the application to aid in the image construction. The feature is not strictly required, see the discussion of building with the GraalVM agent below.

Once built, native images have very fast startup!

This feature supports:

- GraalVM 19.3.1 *1
- Spring Boot 2.2.X

*1 Some of the samples work with Graal 19.3.1, whilst some do not, we are working hard with the GraalVM team to address all issues on both sides.

## Download Graal 
To try it out, install GraalVM 19.3.1 from: [https://github.com/graalvm/graalvm-ce-builds/releases](),
then once having set your `PATH` to that new java home, install `native-image` by running `gu install native-image`.

## Building the feature
Then in the top level of your clone of this repository, build the feature project with `./build-feature.sh`.

## Samples 
In the `spring-graal-native-samples` subfolder from the root of the
project. Each folder in there is using a piece of Spring technology. Within each
is a mini project and a `compile.sh` script - the script will call the 
native-image command passing the feature on the classpath, the executable produced
in each case should start instantly. You can use also `test.sh` to validate the native
image works as expected. `build.sh` invokes both `compile.sh` and `test.sh`.

You can build all samples by running `./build-samples.sh` in the project root. Warning: this may highlight issues with a couple that we are working to fix!

You can try the feature with your own projects, see the sample projects for typical usage or the `commandlinerunner-maven` sample for how to add native-image packaging to your pom.xml. 

## FAQ

*Q. I get out of memory problems?*

A. native-image likes to use a _lot_ of RAM. There have been problems observed at 8G.


*Q. This just isn't working for my project! Why not?*

A. As samples are added, new technologies/libraries are supported. Initially support
might be hard coded for that specific sample but the intention is always to generalize
that support so any project using 'that tech' will work. So, if not working for
your project it may be because you are using a library that hasn't been tested
yet (no sample) or we haven't yet generalized the support for that library to
work for any project consuming it. Feel free to let us know and we can explore
what's up.

*Q. Are you taking contributions?*

A. Sure, but please recognize this is an experimental project, we aren't at the
   polishing javadoc stage right now :)

## Internal notes:

As presented at [Spring One Platform 2019](https://www.youtube.com/watch?v=OxS66Q26ykA) the feature will optimize the application as the native-image is built. It will discard autoconfiguration that is never going to be used (because at image build time we can make closed world assumptions). This further improves startup time and reduces memory consumption.

## Feature Options

These can be specified on the call to native image:

`-Dverbose=true` will output lots of information about the feature behaviour as it processes auto configuration and chooses which to include.

`-Dmode=light` will flip the feature into a lightweight mode that only supplies
substitutions and initialization configuration. It will no longer set reflection, resource or proxy data - relying on the project to supply that. See the `jafu` sample for an example of this usage.

## Running only with the agent provided configuration

When compiling, native-image wants to know specific information about your application so it includes what is necessary in the resulting executable. It needs to know what kinds of types your application is going to reflect on, which resources it is going to load, which types it is going to create proxies for. For Spring applications this is what the feature is trying to compute, but it isn't guaranteed to get it all 100% right now.

The GraalVM team include an agent that can be used to collect this information. This can be used as follows:

* `mkdir -p graal/META-INF/native-image`
* Add `-agentlib:native-image-agent=config-output-dir=graal/META-INF/native-image` to the java command you use to run your application.
* Launch your application and exercise any codepaths you want the native image to support. It will only collect information from code paths that are exercised.
* Shutdown your application. The `graal/META-INF/native-image` folder will contain all the configuration data from that run
* The agent does not know how to setup initialization configuration though - it does not know what could be initialized at
image build time vs run time. For that reason the feature supports a light mode where most configuration is expected
 to come from the agent produced output and the feature only specifies the initialization information (plus any important substitutions).
Running native-image would look a little like this where the classpath is including the feature plus that graal dir in which the configuration was collected:

```
native-image \
  ... \
  -cp $CLASSPATH:graal:<pathToTheFeature>.jar \
  -Dmode=light \
  MyApplication
```
In the `commandlinerunner` sample there is an `agent` subdirectory that shows the steps involved in running with the agent and compiling. Note that the agent does miss a few places right now (we are working with the GraalVM team to address this). For this reason you will see a patch step in the scripts in that folder which adds entries the agent missed.

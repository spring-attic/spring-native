# spring-graal-native

This project contains code to enable the building of native images with GraalVM.

Once built, native images have very fast startup!

This feature supports:
- GraalVM 19.2.1 (19.3 not supported yet)
- Spring Boot 2.2.0

To try it out, install GraalVM 19.2.1 from: https://github.com/oracle/graal/releases,
then install `native-image` by running `gu install native-image` (currently it is
available as an early adopter plugin).


Then build the feature project with `./build-feature.sh`.

Now go into the `spring-graal-native-samples` subfolder from the root of the
project. Each folder in there is using a piece of Spring technology. Within each
is a mini project and a `compile.sh` script - the script will call the 
native-image command passing the feature on the classpath, the executable produced
in each case should start instantly. You can use also `test.sh` to validate the native
image works as expected. `build.sh` invokes both `compile.sh` and `test.sh`.

You can build all samples with `./build-samples.sh`.

You can try the feature with your own projects, see the sample projects for typical
usage or the `commandlinerunner-maven` sample for how to add native-image packaging
to your pom.xml. 

FAQ

Q. I get out of memory problems?

A. native-image likes to use a lot of RAM. There have been problems observed at 8G.


Q. This just isn't working for my project! Why not?

A. As samples are added, new technologies/libraries are supported. Initially support
   might be hard coded for that sample but the intention is always to generalize
   that support so any project using 'that tech' will work. So, if not working for
   your project it may be because you are using a library that hasn't been tested
   yet (no sample) or we haven't yet generalized the support for that library to
   work for any project consuming it. Feel free to let us know and we can explore
   what's up.

Q. Are you taking contributions?

A. Sure, but please recognize this is an experimental project, we aren't at the
   polishing javadoc stage right now :)

Feature Options

These can be specified on the call to native image:

`-Dverbose=true` will output lots of information about the feature behaviour as it
processes auto configuration and chooses which to include.
`-Dmode=light` will flip the feature into a lightweight mode that only supplies
substitutions and initialization configuration. It will no longer set reflection,
resource or proxy data - relying on the project to supply that. See the `jafu`
sample for an example of this usage.


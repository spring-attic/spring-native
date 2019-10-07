# spring-graal-native

This project contains code to enable the building of native-images with Graal VM.

Once built, native-images have very fast startup!

This feature supports:
- Graal 19.2.0.1
- Spring Boot 2.2.0.RC1

To try it out, install Graal 19.2.0.1 from: https://github.com/oracle/graal/releases


Then build the feature project with:

```
cd spring-graal-native-feature
./mvnw clean package
```

Now go into the spring-graal-native-image-samples subfolder from the root of the
project. Each folder in there is using a piece of Spring technology. Within each
is a mini project and a `compile.sh` script - the script will call the 
native-image command passing the feature on the classpath, the executable produced
in each case should start instantly.

You can try the feature with your own projects, see the sample projects for typical
usage or the commandlinerunner-maven sample for how to add native-image packaging
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

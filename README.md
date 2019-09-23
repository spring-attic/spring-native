# spring-boot-feature

This is a Graal feature than enables Spring applications to be compiled using the Graal
native-image command. Once compiled they will have instant startup!

This feature supports:
- Graal 19.2.0.1
- Spring Boot 2.2.0.BUILD-SNAPSHOT (pre RC1)
- Spring Framework 5.2.0.BUILD-SNAPSHOT (pre GA)

To try it out, install Graal 19.2.0.1 from: https://github.com/oracle/graal/releases

Then build the root feature project with:

`mvn clean package`

Now go into the samples subfolder. Each folder in there is using a piece of Spring
technology. Within each is a mini project and a `compile.sh` script - the script
will call the native-image command passing the feature on the classpath, the executable
produced in each case should start instantly.


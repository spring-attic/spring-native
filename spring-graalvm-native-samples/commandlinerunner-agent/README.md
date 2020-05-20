This is a variant of the commandlinerunner sample that uses Maven and GraalVM tracing agent to drive the native-image construction.

Ensure you have have the graal JDK installed and JAVA_HOME set appropriately.

Then you can:

`mvn clean package`

This will build the project normally and give you a boot executable jar (as normal)

`mvn -Pnative clean package`

This will compile the project then drive it through native-image, producing an executable called `commandlinerunner-agent` in the target folder.

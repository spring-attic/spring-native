This is a variant of the commandlinerunner sample that uses maven to drive the native-image construction.

Ensure you have have the graal JDK installed and JAVA_HOME set appropriately.

Then you can:

`mvn clean package`

This will build the project normally and give you a boot executable jar (as normal)

`mvn -Pgraal clean package`

This will compile the project then drive it through native-image, producing an executable called `clr` in the target folder.

Notes:
- without the compile script we need to pass the options to the native-image command. This is done via the file (currently) in `src/main/resources/META-INF/native-image/com.example/commandlinerunner/native-image.properties`:
- the script also used to pass the initial class to the command. That is now done by setting `<start-class>` in the properties section of the pom.



```
time ./target/commandlinerunner

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::

commandlinerunner running!

real	0m0.050s
user	0m0.030s
sys	0m0.015s
```

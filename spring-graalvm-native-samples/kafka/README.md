## Kafka "Hello, world!" App

Notes:

- I had to add reactor as a dependency, which is normally optional; possibly because we play some tricks to not reference its classes if it's not on the CP.
- I added the config to run the agent with the `contextLoads` test.


State of the art with `mvn clean package -Pnative`:

```
Computed spring.components is
vvv
com.example.kafka.GraalKafka1Application=org.springframework.stereotype.Component
^^^
hybrid: adding access to Type:com/example/kafka/GraalKafka1Application since @SpringBootApplication
Number of types dynamically registered for reflective access: #585
WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration. Reason: java.lang.NoClassDefFoundError: com/fasterxml/jackson/databind/module/SimpleModule.
WARNING: Could not register reflection metadata for org.springframework.boot.diagnostics.analyzer.ValidationExceptionFailureAnalyzer. Reason: java.lang.NoClassDefFoundError: javax/validation/ValidationException.
WARNING: Could not register reflection metadata for org.springframework.boot.liquibase.LiquibaseChangelogMissingFailureAnalyzer. Reason: java.lang.NoClassDefFoundError: liquibase/exception/ChangeLogParseException.
[kafka1:30649]     analysis:  16,984.84 ms,  1.71 GB
Error: type is not available in this platform: org.graalvm.compiler.hotspot.management.AggregatedMemoryPoolBean
Error: Use -H:+ReportExceptionStackTraces to print stacktrace of underlying exception
Error: Image build request failed with exit status 1
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  46.133 s
[INFO] Finished at: 2020-09-03T16:08:49-04:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.graalvm.nativeimage:native-image-maven-plugin:20.2.0:native-image (default) on project kafka1: Execution of /Library/Java/JavaVirtualMachines/graalvm-ce-java11-20.2.0/Contents/Home/bin/native-image -cp /Users/grussell/.m2/repository/org/springframework/boot/spring-boot-starter/2.4.0-M2/spring-boot-starter-2.4.0-M2.jar:/Users/grus
...
```

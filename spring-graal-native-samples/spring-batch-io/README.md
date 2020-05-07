A Spring Boot project using Spring Batch to read from a file and write to another file.

This project is a port of the Spring Batch I/O sample that reads from a CSV and writes to a CSV.

This sample uses Spring Batch's `Step` scope for delayed object creation.  Normally the methods annotated wtih `@StepScope` should return the class to allow for further introspection by the container.  However, since GraalVM does not support dynamic subclassing, users of `Step` or `Job` scopes will be required to manually register their instances based on the functionality required (listeners, etc). 

Current status:
The configuration in src/main/resources/META-INF/native-image was generated with an agent run of the application.
The additional configuration in the graal folder was added as required whilst iterating on trying to get it to work.
The compile script is full of initialize-at-build-time entries.
The setting `spring.aop.proxy-target-class: false` is set in the app - is that valid/correct for this setup?


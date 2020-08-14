A Spring Boot project using Spring Batch to read from a file and write to another file.

This project is a port of the Spring Batch I/O sample that reads from a CSV and writes to a CSV.

This sample uses Spring Batch's `Step` scope for delayed object creation.  Normally the methods annotated wtih `@StepScope` should return the class to allow for further introspection by the container.  However, since GraalVM does not support dynamic subclassing, users of `Step` or `Job` scopes will be required to manually register their instances based on the functionality required (listeners, etc).

TODO:

 - Add proper support via hints
 - Remove manual config entries
 - On Batch side add `proxyBeanMethods=false` to `org.springframework.batch.core.configuration.annotation.ScopeConfiguration` and `org.springframework.batch.core.configuration.annotation.SimpleBatchConfiguration`.  

To build and run the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker-compose up
```

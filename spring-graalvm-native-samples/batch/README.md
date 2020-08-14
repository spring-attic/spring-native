Very basic spring boot project using Spring Batch.

TODO:

 - Add proper support via hints
 - Remove manual config entries
 - On Batch side add `proxyBeanMethods=false` to `org.springframework.batch.core.configuration.annotation.ScopeConfiguration` and `org.springframework.batch.core.configuration.annotation.SimpleBatchConfiguration`.  

To build and run the native application packaged in a lightweight container:
```
mvn spring-boot:build-image
docker-compose up
```
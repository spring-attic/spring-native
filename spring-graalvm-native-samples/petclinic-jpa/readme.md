Spring Boot famous Petclinic sample with JPA persistence.

To build and run the native application packaged in a lightweight container with `default` mode:
```
mvn spring-boot:build-image
docker-compose up
```

To test hsqldb database:

 * update *pom.xml*, set scope of hsqldb dependency as runtime
 * update *docker-compose.yml*, add the following environment variable to petclinic-jpa: *spring_profiles_active=hsqldb*
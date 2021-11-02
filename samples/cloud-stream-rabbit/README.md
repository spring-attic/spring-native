## Spring Cloud Stream Rabbit Binder Native Sample App

To build and run the native application on the JVM:

```
mvn clean spring-boot:build-image
docker-compose up
java -jar target/cloud-stream-rabbit-0.0.1-SNAPSHOT.jar
```

To build the application as a native image:

```
mvn -Pnative -DskipTests package
docker-compose up
./target/cloud-stream-rabbit
```

As an alternative, you can use `build.sh` script that is provided as part of this sample application directory.
This script will start RabbitMQ in a docker container, build the image, run the test that verifies the sample works as expected and then stop the docker container. 

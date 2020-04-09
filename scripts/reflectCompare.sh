# Creates a compact diff between two reflect json files
java -classpath `dirname $0`/../spring-graal-native-feature/target/spring-graal-native-feature-0.7.0.BUILD-SNAPSHOT.jar org.springframework.graal.support.ReflectionJsonComparator $1 $2

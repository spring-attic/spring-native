# Creates a compact diff between two reflect json files
java -classpath `dirname $0`/../spring-graal-native-feature/target/spring-graal-native-feature-0.6.0.RELEASE.jar org.springframework.graal.support.ReflectionJsonComparator $1 $2

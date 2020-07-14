# Creates a compact diff between two reflect json files
java -classpath `dirname $0`/../spring-graalvm-native-feature/target/spring-graalvm-native-tools-*.jar org.springframework.graalvm.support.ReflectionJsonComparator $1 $2

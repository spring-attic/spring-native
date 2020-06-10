# Creates a compact diff between two reflect json files
java -classpath `dirname $0`/../spring-graalvm-native-feature/target/spring-graalvm-native-feature-0.7.0.jar org.springframework.graalvm.support.ReflectionJsonStrip $1 $2

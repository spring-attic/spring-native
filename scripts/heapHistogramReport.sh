# Creates a HTML navigable tree of heap histogram output
java -classpath `dirname $0`/../spring-graalvm-native-tools/target/spring-graalvm-native-tools-*.jar org.springframework.nativex.support.HeapHistogramReport $1

# Creates a HTML navigable tree of heap histogram output
java -classpath `dirname $0`/../spring-native-tools/target/spring-native-tools-*.jar org.springframework.nativex.support.HeapHistogramReport $1

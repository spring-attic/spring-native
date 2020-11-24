# Creates a HTML navigable diff between two sets of PrintHeapHistogram output
java -classpath `dirname $0`/../spring-graalvm-native-tools/target/spring-graalvm-native-tools-*.jar org.springframework.nativex.support.HistogramDiff $1 $2 $3

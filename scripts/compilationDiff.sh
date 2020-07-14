# Creates a HTML navigable diff between two sets of PrintAOTCompilation output
java -classpath `dirname $0`/../spring-graalvm-native-tools/target/spring-graalvm-native-tools-*.jar org.springframework.graalvm.support.CompilationSummaryDiff $1 $2 $3

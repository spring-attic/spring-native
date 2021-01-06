# Creates a HTML navigable diff between two sets of PrintAOTCompilation output
java -classpath `dirname $0`/../spring-native-tools/target/spring-native-tools-*.jar org.springframework.nativex.support.CompilationSummaryDiff $1 $2 $3

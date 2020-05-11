rm -rf target/
mkdir -p target/native-image 2>/dev/null
mvn -B -Pgraal package > target/native-image/output.txt

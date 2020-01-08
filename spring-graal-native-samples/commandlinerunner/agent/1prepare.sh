cd ..
mvn clean package
cd agent
rm -rf unpack
unzip ../target/commandlinerunner-0.0.1-SNAPSHOT.jar -d unpack
cd unpack/BOOT-INF/classes
cp -R ../../META-INF .
mkdir -p graal/META-INF/native-image

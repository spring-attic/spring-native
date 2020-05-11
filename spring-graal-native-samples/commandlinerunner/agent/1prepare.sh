cd ..
mvn -B clean package
cd agent
rm -rf unpack
unzip ../target/commandlinerunner-0.0.1-SNAPSHOT.jar -d unpack
cd unpack/BOOT-INF/classes
cp -R ../../META-INF .
rm -rf graal
mkdir -p graal/META-INF/native-image

mvn clean

cd samples/commandlinerunner
mvn clean
rm clr
rm -rf unpack

cd ../commandlinerunner-maven
mvn clean

cd ../vanilla-grpc
mvn clean
rm -rf unpack
rm grpc

cd ../vanilla-jpa
mvn clean
rm -rf unpack
rm jpa

cd ../vanilla-orm
mvn clean
rm -rf unpack
rm orm

cd ../vanilla-rabbit
mvn clean
rm -rf unpack
rm rabbit

cd ../vanilla-thymeleaf
mvn clean
rm -rf unpack
rm thymeleaf

cd ../vanilla-tx
mvn clean
rm -rf unpack
rm tx

cd ../webflux-netty
mvn clean
rm -rf unpack
rm webflux-netty

cd ../..

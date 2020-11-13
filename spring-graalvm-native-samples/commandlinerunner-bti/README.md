#commandlinerunner-bti

This is a variant of the commandlinerunner sample but using build time
infrastructure processing (a maven plugin) to generate proxies such that
there is no need to set `proxyBeanMethods=false` on the components.

What makes this different to regular commandlinerunner?

The `pom.xml` contains this extra snippet:

```
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.experimental</groupId>
            <artifactId>spring-graalvm-native-maven-plugin</artifactId>
            <version>0.8.3-SNAPSHOT</version>
            <executions>
                <execution>
                    <id>sources</id>
                    <phase>process-sources</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <inherited>false</inherited>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

And when the application builds, there is a new folder `target/generated-sources`
and a folder `target/maven-status`.

It is buildable in all the usual ways:

with scripts - `./build.sh`

with maven as simple native executable - `mvn -Pnative clean package`

with build service as native executable wrapped in container - `mvn clean spring-boot:build-image` then `docker-compose up commandlinerunner-bti`
(I have already seen issues with build-image not behaving and needing a mvn package
before doing the build-image step)

Some of the magic happens in the `LiteConfigurationProcessor` which looks for these generated entities.

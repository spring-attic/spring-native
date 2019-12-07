# Commandlinerunner with agent

- Run: "`./1prepare.sh`" to build the project and unpack the packaged jar

- in this folder run "`. ./2setcp.sh`" to set the classpath (CP)

- this will leave you in `unpack/BOOT-INF/classes`

- run "`../../../3runWithAgent.sh`". This will execute the app with the agent
and populate the json files in `graal/META-INF/native-image` (under `unpack/BOOT-INF/classes`)

NOTE, due to a bug ( https://github.com/oracle/graal/issues/1940 )
at this point the `reflect-config.json` generated in that folder is missing:
`org.springframework.context.support.PropertySourcesPlaceholderConfigurer`

- run the native-image build "`../../../4compile.sh`" this will produce a `clr` executable.

- run `clr`, it will fail with:
```
Caused by: java.lang.ClassNotFoundException: org.springframework.context.support.PropertySourcesPlaceholderConfigurer
	at com.oracle.svm.core.hub.ClassForNameSupport.forName(ClassForNameSupport.java:60) ~[na:na]
	at java.lang.ClassLoader.loadClass(Target_java_lang_ClassLoader.java:131) ~[na:na]
	at org.springframework.boot.autoconfigure.condition.FilteringSpringBootCondition.resolve(FilteringSpringBootCondition.java:108) ~[na:na]
	at org.springframework.boot.autoconfigure.condition.OnBeanCondition$Spec.getReturnType(OnBeanCondition.java:517) ~[na:na]
	at org.springframework.boot.autoconfigure.condition.OnBeanCondition$Spec.deducedBeanTypeForBeanMethod(OnBeanCondition.java:505) ~[na:na]
	... 20 common frames omitted
```

- Modify the generated `graal/META-INF/native-image/reflect-config.json` with:

```
{"name": "org.springframework.context.support.PropertySourcesPlaceholderConfigurer", "allDeclaredConstructors":true},
```

- Now rerun compilation `../../../4compile.sh`

- Now run `clr`:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::

2019-12-05 11:49:46.966  INFO 3273 --- [           main] c.e.c.CommandlinerunnerApplication       : Starting CommandlinerunnerApplication on Andys-MacBook-Pro-2018.local with PID 3273 (/Users/aclement/gits/graal-agent-1/unpack/BOOT-INF/classes/clr started by aclement in /Users/aclement/gits/graal-agent-1/unpack/BOOT-INF/classes)
2019-12-05 11:49:46.966  INFO 3273 --- [           main] c.e.c.CommandlinerunnerApplication       : No active profile set, falling back to default profiles: default
2019-12-05 11:49:46.978  INFO 3273 --- [           main] c.e.c.CommandlinerunnerApplication       : Started CommandlinerunnerApplication in 0.271 seconds (JVM running for 0.273)
CLR running!
```



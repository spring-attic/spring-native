Very basic spring boot project. Using a CommandLineRunner bean.

```
./compile.sh

...
...
...

Java exploded jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::             (v2.2.0.M2)

2019-05-17 15:06:11.593  INFO 91777 --- [           main] c.e.c.CommandlinerunnerApplication       : Starting CommandlinerunnerApplication on Andys-MacBook-Pro-2018.local with PID 91777 (/Users/aclement/gits/spring-boot-graal-feature/samples/commandlinerunner/unpack/BOOT-INF/classes started by aclement in /Users/aclement/gits/spring-boot-graal-feature/samples/commandlinerunner/unpack/BOOT-INF/classes)
2019-05-17 15:06:11.596  INFO 91777 --- [           main] c.e.c.CommandlinerunnerApplication       : No active profile set, falling back to default profiles: default
2019-05-17 15:06:11.930  INFO 91777 --- [           main] c.e.c.CommandlinerunnerApplication       : Started CommandlinerunnerApplication in 0.602 seconds (JVM running for 0.877)
CLR running!

real	0m0.935s
user	0m1.925s
sys	0m0.225s


Compiled app (clr)

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::

May 17, 2019 3:06:12 PM org.springframework.boot.StartupInfoLogger logStarting
INFO: Starting CommandlinerunnerApplication on Andys-MacBook-Pro-2018.local with PID 91778 (started by aclement in /Users/aclement/gits/spring-boot-graal-feature/samples/commandlinerunner)
May 17, 2019 3:06:12 PM org.springframework.boot.SpringApplication logStartupProfileInfo
INFO: No active profile set, falling back to default profiles: default
May 17, 2019 3:06:12 PM org.springframework.boot.StartupInfoLogger logStarted
INFO: Started CommandlinerunnerApplication in 0.029 seconds (JVM running for 0.03)
CLR running!

real	0m0.223s
user	0m0.023s
sys	0m0.010s
```

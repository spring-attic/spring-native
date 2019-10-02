Very basic spring boot project. Using a CommandLineRunner bean.

Run the `./compile.sh` script to run the maven build and run native-image on it.

Then you can launch the `./clr` executable:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::

Sep 05, 2019 8:53:20 AM org.springframework.boot.StartupInfoLogger logStarting
INFO: Starting CommandlinerunnerApplication on Andys-MacBook-Pro-2018.local with PID 18483 (/Users/aclement/gits3/spring-graal-feature/samples/commandlinerunner/clr started by aclement in /Users/aclement/gits3/spring-graal-feature/samples/commandlinerunner)
Sep 05, 2019 8:53:20 AM org.springframework.boot.SpringApplication logStartupProfileInfo
INFO: No active profile set, falling back to default profiles: default
Sep 05, 2019 8:53:20 AM org.springframework.boot.StartupInfoLogger logStarted
INFO: Started CommandlinerunnerApplication in 0.045 seconds (JVM running for 0.048)
CLR running!
```

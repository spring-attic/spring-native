# Commandlinerunner with agent

There is a `build.sh` script that will run all these steps automatically, but this gives a breakdown of what
`.build.sh` actually does:

- Runs `./1prepare.sh` to build the project and unpack the packaged jar

- Runs `. ./2setcp.sh` in this folder to set the classpath and leave you in `unpack/BOOT-INF/classes`

- Runs `../../../3runWithAgent.sh`. This will execute the app with the agent
and populate the json files in `graal/META-INF/native-image` (under `unpack/BOOT-INF/classes`)

NOTE, due to a bug ( https://github.com/oracle/graal/issues/1940 )
at this point the `reflect-config.json` generated in that folder is missing:
`org.springframework.context.support.PropertySourcesPlaceholderConfigurer`

- Runs `../../../4patch.sh` this will insert the missing entry

- Runs `../../../5compile.sh` to compile the application to a native-image called `commandlinerunner`

- Runs the `test.sh` script to test `commandlinerunner` and produce stats in a `summary.csv` file

How to run this app with 'just the agent' output.

1. Run `1prepare.sh` - this will unzip the project into the unpack folder

2. Run `. ./2setcp.sh` - this will set your classpath and drop you into unpack/BOOT-INF/classes

3. Run `../../../3runWithAgent.sh` - this will run your app with the agent and dump the config data into graal/META-INF/native-image. MAKE SURE YOU EXERCISE THE APP (curl the endpoints, whatever). Ctrl+C the app

4. Because the agent is missing places (see bug#1940) run `../../../4runWithAgent2.sh 2>&1 | tee output.txt` - this will run the app with AspectJ loadtime weaving an aspect to catch the places the agent missed and dump them out. Again, make sure you exercise the app whilst it is up. Ctrl+C the app.

5. Run `grep allDeclaredMethods output.txt | sort | uniq | pbcopy` (use something other than pbcopy if not on mac). Paste that content onto line 2 of the graal/META-INF/native-image/reflect-config.json

6. This still misses two things, paste these in yourself to the same reflect-config.json file:
```
{"name":"org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator","allDeclaredConstructors":true,"allDeclaredMethods":true},
{"name":"org.apache.catalina.authenticator.jaspic.AuthConfigFactoryImpl","allDeclaredConstructors":true,"allDeclaredMethods":true},

```

7. Run `../../../4compile.sh` to finally run native-image and give you an `st` executable


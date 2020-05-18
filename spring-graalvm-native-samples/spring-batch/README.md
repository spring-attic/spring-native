Very basic spring boot project using Spring Batch.

Current status:
The configuration in src/main/resources/META-INF/native-image was generated with an agent run of the application.
The additional configuration in the graal folder was added as required whilst iterating on trying to get it to work.
The compile script is full of initialize-at-build-time entries.
The setting `spring.aop.proxy-target-class: false` is set in the app - is that valid/correct for this setup?


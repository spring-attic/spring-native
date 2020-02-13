package org.springframework.boot.autoconfigure.task;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/*
proposedHints.put("Lorg/springframework/boot/autoconfigure/task/TaskExecutionAutoConfiguration;",
		new CompilationHint(true,false, new String[] {
				"org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor:REGISTRAR"
		}));
		*/
@ConfigurationHint(value=TaskExecutionAutoConfiguration.class, typeInfos = {
		@TypeInfo(types= {ThreadPoolTaskExecutor.class},access=AccessBits.CLASS|AccessBits.PUBLIC_METHODS|AccessBits.PUBLIC_CONSTRUCTORS)
},abortIfTypesMissing = true)
public class Hints implements NativeImageConfiguration {
}

package org.springframework.boot.autoconfigure.session;

import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration.ReactiveSessionConfigurationImportSelector;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration.ServletSessionConfigurationImportSelector;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration.SessionConfigurationImportSelector;
import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;

/*
proposedHints.put("Lorg/springframework/boot/autoconfigure/session/SessionAutoConfiguration$ReactiveSessionConfigurationImportSelector;",
		new CompilationHint(true, true, new String[] {
				"org.springframework.boot.autoconfigure.session.RedisReactiveSessionConfiguration",
				"org.springframework.boot.autoconfigure.session.MongoReactiveSessionConfiguration",
				"org.springframework.boot.autoconfigure.session.NoOpReactiveSessionConfiguration"
		}));
		*/
@ConfigurationHint(value=ReactiveSessionConfigurationImportSelector.class, typeInfos= {
	@TypeInfo(types= {RedisReactiveSessionConfiguration.class, MongoReactiveSessionConfiguration.class, NoOpReactiveSessionConfiguration.class})	
},abortIfTypesMissing = true,follow=true) // follow should be per entry and obvious as these are configurations
/*
proposedHints.put("Lorg/springframework/boot/autoconfigure/session/SessionAutoConfiguration$SessionConfigurationImportSelector;",
		new CompilationHint(true, true, new String[] {
				"org.springframework.boot.autoconfigure.session.RedisSessionConfiguration",
				"org.springframework.boot.autoconfigure.session.RedisReactiveSessionConfiguration",
				"org.springframework.boot.autoconfigure.session.MongoSessionConfiguration",
				"org.springframework.boot.autoconfigure.session.MongoReactiveSessionConfiguration",
				"org.springframework.boot.autoconfigure.session.JdbcSessionConfiguration",
				"org.springframework.boot.autoconfigure.session.HazelcastSessionConfiguration",
				"org.springframework.boot.autoconfigure.session.NoOpSessionConfiguration",
				"org.springframework.boot.autoconfigure.session.NoOpReactiveSessionConfiguration"
		}));
		*/
@ConfigurationHint(value=SessionConfigurationImportSelector.class, typeInfos= {
	@TypeInfo(types= {RedisSessionConfiguration.class, RedisReactiveSessionConfiguration.class, MongoSessionConfiguration.class, MongoReactiveSessionConfiguration.class,
		JdbcSessionConfiguration.class,HazelcastSessionConfiguration.class,NoOpSessionConfiguration.class,NoOpReactiveSessionConfiguration.class	
	})
},abortIfTypesMissing = true,follow = true)
/*
proposedHints.put("Lorg/springframework/boot/autoconfigure/session/SessionAutoConfiguration$ServletSessionConfigurationImportSelector;",
		new CompilationHint(true, true, new String[] {
				"org.springframework.boot.autoconfigure.session.RedisSessionConfiguration",
				"org.springframework.boot.autoconfigure.session.MongoSessionConfiguration",
				"org.springframework.boot.autoconfigure.session.JdbcSessionConfiguration",
				"org.springframework.boot.autoconfigure.session.HazelcastSessionConfiguration",
				"org.springframework.boot.autoconfigure.session.NoOpSessionConfiguration"
		}));
*/
@ConfigurationHint(value=ServletSessionConfigurationImportSelector.class, typeInfos= {
	@TypeInfo(types= {RedisSessionConfiguration.class,MongoSessionConfiguration.class,
					  JdbcSessionConfiguration.class,HazelcastSessionConfiguration.class,NoOpSessionConfiguration.class	
					})
},abortIfTypesMissing=true,follow=true)
public class Hints implements NativeImageConfiguration {
}

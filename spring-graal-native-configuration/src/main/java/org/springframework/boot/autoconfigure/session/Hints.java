/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.autoconfigure.session;

import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration.ReactiveSessionConfigurationImportSelector;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration.ServletSessionConfigurationImportSelector;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration.SessionConfigurationImportSelector;
import org.springframework.graal.extension.NativeImageHint;
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
@NativeImageHint(trigger=ReactiveSessionConfigurationImportSelector.class, typeInfos= {
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
@NativeImageHint(trigger=SessionConfigurationImportSelector.class, typeInfos= {
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
@NativeImageHint(trigger=ServletSessionConfigurationImportSelector.class, typeInfos= {
	@TypeInfo(types= {RedisSessionConfiguration.class,MongoSessionConfiguration.class,
					  JdbcSessionConfiguration.class,HazelcastSessionConfiguration.class,NoOpSessionConfiguration.class	
					})
},abortIfTypesMissing=true,follow=true)
public class Hints implements NativeImageConfiguration {
}

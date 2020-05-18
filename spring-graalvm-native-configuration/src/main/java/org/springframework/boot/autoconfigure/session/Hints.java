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
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.TypeInfo;

@NativeImageHint(trigger=ReactiveSessionConfigurationImportSelector.class, typeInfos= {
	@TypeInfo(types= {RedisReactiveSessionConfiguration.class, MongoReactiveSessionConfiguration.class, NoOpReactiveSessionConfiguration.class})	
},abortIfTypesMissing = true,follow=true) // follow should be per entry and obvious as these are configurations
@NativeImageHint(trigger=SessionConfigurationImportSelector.class, typeInfos= {
	@TypeInfo(types= {RedisSessionConfiguration.class, RedisReactiveSessionConfiguration.class, MongoSessionConfiguration.class, MongoReactiveSessionConfiguration.class,
		JdbcSessionConfiguration.class,HazelcastSessionConfiguration.class,NoOpSessionConfiguration.class,NoOpReactiveSessionConfiguration.class	
	})
},abortIfTypesMissing = true,follow = true)
@NativeImageHint(trigger=ServletSessionConfigurationImportSelector.class, typeInfos= {
	@TypeInfo(types= {RedisSessionConfiguration.class,MongoSessionConfiguration.class,
					  JdbcSessionConfiguration.class,HazelcastSessionConfiguration.class,NoOpSessionConfiguration.class	
					})
},abortIfTypesMissing=true,follow=true)
public class Hints implements NativeImageConfiguration {
}

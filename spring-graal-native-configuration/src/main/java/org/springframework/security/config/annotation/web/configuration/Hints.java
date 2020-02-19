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
package org.springframework.security.config.annotation.web.configuration;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.web.servlet.DispatcherServlet;

/*
proposedHints.put(SpringWebMvcImportSelector,
		new CompilationHint(false, true, new String[] {
			"org.springframework.web.servlet.DispatcherServlet:EXISTENCE_CHECK",
			"org.springframework.security.config.annotation.web.configuration.WebMvcSecurityConfiguration"
		}));
		*/
@ConfigurationHint(value=SpringWebMvcImportSelector.class,follow = true, typeInfos= {
		@TypeInfo(types= {DispatcherServlet.class},access=AccessBits.CLASS),
		@TypeInfo(types= {WebMvcSecurityConfiguration.class})
})
/*
		// TODO this one is actually incomplete, finish it
public final static String OAuth2ImportSelector = "Lorg/springframework/security/config/annotation/web/configuration/OAuth2ImportSelector;";
proposedHints.put(OAuth2ImportSelector,
		new CompilationHint(false, true, new String[] {
			"org.springframework.security.oauth2.client.registration.ClientRegistration:EXISTENCE_CHECK",
			"org.springframework.security.config.annotation.web.configuration.OAuth2ClientConfiguration"
		}));
*/
@ConfigurationHint(value=OAuth2ImportSelector.class,follow = true, typeInfos= {
		@TypeInfo(types= {ClientRegistration.class},access=AccessBits.CLASS),
		@TypeInfo(types= {OAuth2ClientConfiguration.class})
})
public class Hints implements NativeImageConfiguration {
}

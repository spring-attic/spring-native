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

package org.springframework.boot.actuate.autoconfigure.security.servlet;

import org.springframework.nativex.extension.NativeImageConfiguration;
import org.springframework.nativex.extension.NativeImageHint;
import org.springframework.nativex.extension.TypeInfo;
import org.springframework.nativex.type.AccessBits;

@NativeImageHint(trigger= ManagementWebSecurityAutoConfiguration.class,typeInfos= {
@TypeInfo(typeNames = {
		"org.springframework.boot.autoconfigure.security.DefaultWebSecurityCondition",
		"org.springframework.boot.autoconfigure.security.DefaultWebSecurityCondition$Classes",
		"org.springframework.boot.autoconfigure.security.DefaultWebSecurityCondition$Beans",
}, access = AccessBits.ALL)})
public class ManagementWebSecurityAutoConfigurationHints implements NativeImageConfiguration {
}

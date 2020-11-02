package org.springframework.boot.actuate.autoconfigure.security.servlet;

import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

@NativeImageHint(trigger= ManagementWebSecurityAutoConfiguration.class,typeInfos= {
@TypeInfo(typeNames = {
		"org.springframework.boot.autoconfigure.security.DefaultWebSecurityCondition",
		"org.springframework.boot.autoconfigure.security.DefaultWebSecurityCondition$Classes",
		"org.springframework.boot.autoconfigure.security.DefaultWebSecurityCondition$Beans",
}, access = AccessBits.ALL)})
public class ManagementWebSecurityAutoConfigurationHints implements NativeImageConfiguration {
}

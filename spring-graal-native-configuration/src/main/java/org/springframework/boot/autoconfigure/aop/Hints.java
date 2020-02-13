package org.springframework.boot.autoconfigure.aop;

import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyProcessorSupport;
import org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.graal.type.AccessBits;

@ConfigurationHint(value=AopAutoConfiguration.class,follow=true,
	typeInfos = {@TypeInfo(types= {
			ProxyProcessorSupport.class,ProxyConfig.class,InfrastructureAdvisorAutoProxyCreator.class,
			AbstractAdvisorAutoProxyCreator.class,AbstractAutoProxyCreator.class
			},access=AccessBits.CLASS|AccessBits.PUBLIC_CONSTRUCTORS|AccessBits.PUBLIC_METHODS)})

public class Hints implements NativeImageConfiguration { }
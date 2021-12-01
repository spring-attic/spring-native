/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.aop;

import java.lang.reflect.Proxy;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.weaver.reflect.Java15AnnotationFinder;
import org.aspectj.weaver.reflect.Java15GenericSignatureInformationProvider;
import org.aspectj.weaver.reflect.Java15ReflectionBasedReferenceTypeDelegate;

import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyProcessorSupport;
import org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = AopAutoConfiguration.class, types = {
		@TypeHint(types = {
				AnnotationAwareAspectJAutoProxyCreator.class,
				AspectJAwareAdvisorAutoProxyCreator.class,
				EnableAspectJAutoProxy.class
		}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS}),
})
@NativeHint(trigger = AopAutoConfiguration.AspectJAutoProxyingConfiguration.class,
	types = {
		@TypeHint(types = { ProxyConfig.class, ProxyProcessorSupport.class }, access = { TypeAccess.DECLARED_FIELDS, TypeAccess.PUBLIC_METHODS}),
		@TypeHint(types = { AbstractAdvisorAutoProxyCreator.class, AbstractAutoProxyCreator.class }, access = TypeAccess.PUBLIC_METHODS),
		@TypeHint(types= InfrastructureAdvisorAutoProxyCreator.class,
				access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS}),
		@TypeHint(types = Proxy.class, access = TypeAccess.DECLARED_METHODS), // aspect on proxied bean such as repository
		@TypeHint(types = {
			Java15AnnotationFinder.class, Java15GenericSignatureInformationProvider.class,
			Java15ReflectionBasedReferenceTypeDelegate.class})
	},
	resources = @ResourceHint(patterns = "org.aspectj.weaver.weaver-messages", isBundle = true) // messages in debug log
)
@NativeHint(trigger = Aspect.class, types = @TypeHint(types = {
		Aspect.class,
		Pointcut.class,
		Before.class,
		AfterReturning.class,
		After.class,
		Around.class
}))
public class AopHints implements NativeConfiguration {
}

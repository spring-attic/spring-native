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
package org.springframework.boot.autoconfigure.validation;

import org.apache.logging.log4j.message.DefaultFlowMessageFactory;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.internal.engine.resolver.JPATraversableResolver;
import org.hibernate.validator.internal.engine.resolver.TraversableResolvers;
import org.hibernate.validator.internal.util.logging.Messages;
import org.hibernate.validator.internal.xml.config.ValidationBootstrapParameters;
import org.hibernate.validator.messageinterpolation.AbstractMessageInterpolator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

@NativeImageHint(trigger=ValidationAutoConfiguration.class,
	typeInfos = {@TypeInfo(types = { 
			ParameterMessageInterpolator.class,
			HibernateValidatorConfiguration.class,
			AbstractMessageInterpolator.class,
			JPATraversableResolver.class,
			TraversableResolvers.class,
			ValidationBootstrapParameters.class,
			PlatformResourceBundleLocator.class,
			ConfigurationImpl.class,
			Messages.class,
			ParameterizedMessageFactory.class,
			DefaultFlowMessageFactory.class
		}, 
	typeNames = {
		"org.hibernate.validator.internal.util.logging.Log_$logger",
		"org.hibernate.validator.internal.util.logging.Log"
	},
	access = AccessBits.CLASS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_CONSTRUCTORS) })
public class Hints implements NativeImageConfiguration {
}

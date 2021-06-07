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

package org.springframework.data;

import java.util.Properties;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.core.io.InputStreamSource;
import org.springframework.data.jpa.repository.support.EntityManagerBeanDefinitionRegistrarPostProcessor;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.PropertiesBasedNamedQueries;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.data.repository.core.support.RepositoryFragmentsFactoryBean;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.AccessBits;

@NativeHint(trigger = AbstractRepositoryConfigurationSourceSupport.class,
		types = {
				@TypeHint(types = {
						RepositoryFactoryBeanSupport.class,
						RepositoryFragmentsFactoryBean.class,
						RepositoryFragment.class,
						TransactionalRepositoryFactoryBeanSupport.class,
						QueryByExampleExecutor.class,
						MappingContext.class,
						RepositoryMetadata.class,
						PropertiesBasedNamedQueries.class
				}),
				@TypeHint(types = {Properties.class, BeanFactory.class, InputStreamSource[].class}, access = AccessBits.CLASS),
				@TypeHint(types = Throwable.class, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_FIELDS),
				@TypeHint(typeNames = {
						"org.springframework.data.projection.SpelEvaluatingMethodInterceptor$TargetWrapper",
				}, access = AccessBits.ALL)
		},
		jdkProxies = @JdkProxyHint(typeNames = {
				"org.springframework.data.annotation.QueryAnnotation",
				"org.springframework.core.annotation.SynthesizedAnnotation" }
		),
		initialization = @InitializationHint(types = AbstractMappingContext.class, initTime = InitializationTime.BUILD)
)
@TypeHint(types= {
		EntityManagerBeanDefinitionRegistrarPostProcessor.class
}, access = AccessBits.CLASS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_CONSTRUCTORS)
public class SpringDataCommonsHints implements NativeConfiguration {

}

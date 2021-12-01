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
import org.springframework.core.io.InputStreamSource;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.PropertiesBasedNamedQueries;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.data.repository.core.support.RepositoryFragmentsFactoryBean;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.TypeHint;

@NativeHint(trigger = RepositoryFactoryBeanSupport.class,
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
				@TypeHint(types = {ReadingConverter.class, WritingConverter.class}),
				@TypeHint(types = {Properties.class, BeanFactory.class, InputStreamSource[].class}),
				@TypeHint(types = Throwable.class, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_FIELDS}),
				@TypeHint(typeNames = {
						"org.springframework.data.projection.SpelEvaluatingMethodInterceptor$TargetWrapper",
				}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS, TypeAccess.PUBLIC_METHODS, TypeAccess.RESOURCE})
		},
		jdkProxies = @JdkProxyHint(typeNames = {
				"org.springframework.data.annotation.QueryAnnotation",
				"org.springframework.core.annotation.SynthesizedAnnotation" }
		),
		initialization = @InitializationHint(types = AbstractMappingContext.class, initTime = InitializationTime.BUILD)
)
public class SpringDataCommonsHints implements NativeConfiguration {
}

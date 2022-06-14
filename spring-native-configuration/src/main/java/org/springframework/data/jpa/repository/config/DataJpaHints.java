/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.data.jpa.repository.config;

import javax.persistence.NamedEntityGraph;

import org.springframework.beans.factory.aspectj.AnnotationBeanConfigurerAspect;
import org.springframework.context.annotation.Import;
import org.springframework.data.DataAuditingHints;
import org.springframework.data.DataNonReactiveAuditingHints;
import org.springframework.data.jpa.domain.support.AuditingBeanFactoryPostProcessor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@Import(DataAuditingHints.class)
@NativeHint(
		trigger = AuditingBeanFactoryPostProcessor.class,
		types = @TypeHint(types = {
				AnnotationBeanConfigurerAspect.class,
				AuditingBeanFactoryPostProcessor.class,
				AuditingEntityListener.class
		}),
		imports = DataNonReactiveAuditingHints.class
)
@NativeHint(
		trigger = JpaRepositoryFactory.class,
		jdkProxies = {
				@JdkProxyHint(types = {
						org.springframework.data.jpa.repository.support.CrudMethodMetadata.class,
						org.springframework.aop.SpringProxy.class,
						org.springframework.aop.framework.Advised.class,
						org.springframework.core.DecoratingProxy.class
				})
		}

)
@NativeHint(
		// https://github.com/spring-projects-experimental/spring-native/issues/1619
		trigger = EntityGraph.class,
		types = @TypeHint(types = NamedEntityGraph.class, access = {})
)
public class DataJpaHints implements NativeConfiguration {

}

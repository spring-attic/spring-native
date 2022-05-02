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

package org.springframework.boot.autoconfigure.data.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.JpaQueryMethodFactory;
import org.springframework.data.jpa.repository.support.JpaEvaluationContextExtension;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.orm.jpa.SharedEntityManagerCreator;

@NativeHint(trigger = JpaRepositoryFactoryBean.class,
		resources = @ResourceHint(patterns = "META-INF/jpa-named-queries.properties"),
		types = {
			@TypeHint(types = {
						SharedEntityManagerCreator.class, // TODO is this one in the right place?
						JpaRepositoryFactoryBean.class,
						JpaEvaluationContextExtension.class,
						JpaQueryMethodFactory.class
				} , typeNames = {
						"org.springframework.data.jpa.repository.config.JpaMetamodelMappingContextFactoryBean",
						"org.springframework.data.jpa.util.JpaMetamodelCacheCleanup"
				}, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_METHODS }),
				@TypeHint(types = Query.class)
		},
		jdkProxies = @JdkProxyHint(typeNames = {
				"org.springframework.data.jpa.repository.support.CrudMethodMetadata",
				"org.springframework.aop.SpringProxy",
				"org.springframework.aop.framework.Advised",
				"org.springframework.core.DecoratingProxy"
		})
)
public class JpaRepositoriesHints implements NativeConfiguration {

}

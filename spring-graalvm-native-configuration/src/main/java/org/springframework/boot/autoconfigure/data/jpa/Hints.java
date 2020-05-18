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
package org.springframework.boot.autoconfigure.data.jpa;

import org.springframework.data.jpa.repository.support.JpaEvaluationContextExtension;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.support.PropertiesBasedNamedQueries;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFragmentsFactoryBean;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.orm.jpa.SharedEntityManagerCreator;

@NativeImageHint(trigger=JpaRepositoriesAutoConfiguration.class,typeInfos= {
		@TypeInfo(types= {
				SharedEntityManagerCreator.class, // TODO is this one in the right place?
				JpaRepositoryFactoryBean.class,
				RepositoryFactoryBeanSupport.class,
				TransactionalRepositoryFactoryBeanSupport.class,
				JpaEvaluationContextExtension.class,
				PropertiesBasedNamedQueries.class, // TODO what is the right trigger for this one? Basically, are there other auto configs that trigger which would need it?
				RepositoryFragmentsFactoryBean.class // TODO ditto
		},typeNames= {
				"org.springframework.data.jpa.repository.config.JpaMetamodelMappingContextFactoryBean",
				"org.springframework.data.jpa.util.JpaMetamodelCacheCleanup"
				},access=AccessBits.CLASS|AccessBits.DECLARED_METHODS|AccessBits.DECLARED_CONSTRUCTORS)
	})
// TODO Why can't I make this conditional on JpaReposAutoConfig above? The vanilla-orm sample needs this but JpaRepositoriesAutoConfiguration is not active in that sample
//@ConfigurationHint(typeInfos= {@TypeInfo(types= {PersistenceAnnotationBeanPostProcessor.class})}) // temporarily moved this to be HibernateJpaConfiguration dependant
public class Hints implements NativeImageConfiguration {
}

/*
 * Copyright 2019 Contributors
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
package org.springframework.orm.jpa.persistenceunit;

import java.net.URL;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.OnlyPresent;

/**
 * Workaround for
 * 
 * Caused by: javax.persistence.PersistenceException: Unable to resolve persistence unit
 * root URL at
 * org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager.determineDefaultPersistenceUnitRootUrl(DefaultPersistenceUnitManager.java:640)
 * at
 * org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager.preparePersistenceUnitInfos(DefaultPersistenceUnitManager.java:462)
 * at
 * org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager.afterPropertiesSet(DefaultPersistenceUnitManager.java:443)
 * at
 * org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.afterPropertiesSet(LocalContainerEntityManagerFactoryBean.java:328)
 * at
 * org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods(AbstractAutowireCapableBeanFactory.java:1841)
 * at
 * org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1778)
 * ... 16 more Caused by: java.io.FileNotFoundException: class path resource [] cannot be
 * resolved to URL because it does not exist at
 * org.springframework.core.io.ClassPathResource.getURL(ClassPathResource.java:195) at
 * org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager.determineDefaultPersistenceUnitRootUrl(DefaultPersistenceUnitManager.java:636)
 * @author Andy Clement
 */
@TargetClass(className = "org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager", onlyWith = OnlyPresent.class)
public final class Target_DefaultPersistenceUnitManager {

	@Substitute
	public URL determineDefaultPersistenceUnitRootUrl() {
		return null;
	}
}

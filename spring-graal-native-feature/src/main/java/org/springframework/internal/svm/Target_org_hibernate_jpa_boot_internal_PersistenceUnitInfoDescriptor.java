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
package org.springframework.internal.svm;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * Workaround for
 * Caused by: com.oracle.svm.core.jdk.UnsupportedFeatureError: Unsupported method java.lang.ClassLoader.registerAsParallelCapable() is reachable: The declaring class of this element has been substituted, but this element is not present in the substitution class
	at com.oracle.svm.core.util.VMError.unsupportedFeature(VMError.java:102)
	at java.lang.ClassLoader.registerAsParallelCapable(Target_java_lang_ClassLoader.java:1204)
	at org.springframework.core.DecoratingClassLoader.<clinit>(DecoratingClassLoader.java:38)
	at com.oracle.svm.core.hub.ClassInitializationInfo.invokeClassInitializer(ClassInitializationInfo.java:347)
	at com.oracle.svm.core.hub.ClassInitializationInfo.initialize(ClassInitializationInfo.java:267)
	at java.lang.Class.ensureInitialized(DynamicHub.java:437)
	at com.oracle.svm.core.hub.ClassInitializationInfo.initialize(ClassInitializationInfo.java:232)
	at java.lang.Class.ensureInitialized(DynamicHub.java:437)
	at com.oracle.svm.core.hub.ClassInitializationInfo.initialize(ClassInitializationInfo.java:232)
	at java.lang.Class.ensureInitialized(DynamicHub.java:437)
	at org.springframework.orm.jpa.persistenceunit.SpringPersistenceUnitInfo.getNewTempClassLoader(SpringPersistenceUnitInfo.java:93)
	at org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor.getTempClassLoader(PersistenceUnitInfoDescriptor.java:78)
	at org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl.populate(EntityManagerFactoryBuilderImpl.java:833)
	at org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl.<init>(EntityManagerFactoryBuilderImpl.java:219)
	at org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl.<init>(EntityManagerFactoryBuilderImpl.java:167)
	at org.springframework.orm.jpa.vendor.SpringHibernateJpaPersistenceProvider.createContainerEntityManagerFactory(SpringHibernateJpaPersistenceProvider.java:51)
	at org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.createNativeEntityManagerFactory(LocalContainerEntityManagerFactoryBean.java:365)
	at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.buildNativeEntityManagerFactory(AbstractEntityManagerFactoryBean.java:390)

 * @author Andy Clement
 */
@TargetClass(className="org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor", onlyWith = OnlyPresent.class)
public final class Target_org_hibernate_jpa_boot_internal_PersistenceUnitInfoDescriptor {

	@Substitute
	public ClassLoader getTempClassLoader() {
		return  null;
	}
}

package org.hibernate.jpa.boot.internal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import org.springframework.graalvm.substitutions.OnlyIfPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl", onlyWith = { OnlyIfPresent.class, RemoveXmlSupport.class })
final class Target_EntityManagerFactoryBuilderImpl {

	@Substitute
	private void processHibernateConfigXmlResources(StandardServiceRegistryBuilder ssrBuilder,
			Target_MergedSettings mergedSettings,
			String cfgXmlResourceName) {
	}

	@TargetClass(className = "org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl$MergedSettings")
	private static final class Target_MergedSettings {
	}
}

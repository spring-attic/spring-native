package org.springframework.orm.jpa.support;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.nativex.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor", onlyWith = { OnlyIfPresent.class, RemoveXmlSupport.class })
final class Target_PersistenceAnnotationBeanPostProcessor {

	@Substitute
	protected <T> T lookup(String jndiName, Class<T> requiredType) throws Exception {
		throw new UnsupportedOperationException("PersistenceAnnotationBeanPostProcessor JNDI support disabled as XML is disabled via spring.xml.ignore flag");
	}

}

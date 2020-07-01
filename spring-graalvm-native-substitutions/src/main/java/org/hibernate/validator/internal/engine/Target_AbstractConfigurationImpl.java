package org.hibernate.validator.internal.engine;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.OnlyIfPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.hibernate.validator.internal.engine.AbstractConfigurationImpl", onlyWith = { OnlyIfPresent.class, RemoveXmlSupport.class })
final class Target_AbstractConfigurationImpl {

	@Substitute
	private void parseValidationXml() {
	}
}

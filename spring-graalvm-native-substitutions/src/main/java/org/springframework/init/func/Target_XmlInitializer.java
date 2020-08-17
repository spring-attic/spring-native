package org.springframework.init.func;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.graalvm.substitutions.OnlyIfPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.springframework.init.func.XmlInitializer", onlyWith = { OnlyIfPresent.class, RemoveXmlSupport.class })
final class Target_XmlInitializer {

	@Substitute
	public void initialize(GenericApplicationContext context) {
	}
}

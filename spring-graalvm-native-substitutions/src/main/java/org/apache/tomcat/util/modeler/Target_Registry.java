package org.apache.tomcat.util.modeler;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.nativex.substitutions.OnlyIfPresent;

// To avoid Registry instantiation and JmxMBeanServer usage
@TargetClass(className = "org.apache.tomcat.util.modeler.Registry", onlyWith = { OnlyIfPresent.class })
final class Target_Registry {

	@Alias
	private static Registry registry = null;

	@Alias
	private Object guard;

	@Substitute
	public static synchronized Registry getRegistry(Object key, Object guard) {
		if (registry == null) {
			disableRegistry();
		}
		return registry;
	}

	@Alias
	public static synchronized void disableRegistry() {
	}
}

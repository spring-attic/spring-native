package org.hibernate.cfg;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.hibernate.bytecode.internal.none.BytecodeProviderImpl;
import org.hibernate.bytecode.spi.BytecodeProvider;

import org.springframework.nativex.substitutions.OnlyIfPresent;

// To avoid to get Byte Buddy and Javassist classes in the native image
@TargetClass(className = "org.hibernate.cfg.Environment", onlyWith = { OnlyIfPresent.class })
final class Target_Environment {

	@Substitute
	private static BytecodeProvider buildBytecodeProvider(String providerName) {
		return new BytecodeProviderImpl();
	}
}

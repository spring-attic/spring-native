package org.springframework.data.mapping.model;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveCglibSupport;

@TargetClass(className = "org.springframework.data.mapping.model.ClassGeneratingEntityInstantiator", onlyWith = { OnlyPresent.class, RemoveCglibSupport.class })
final class Target_ClassGeneratingEntityInstantiator {

	@Substitute
	private EntityInstantiator createEntityInstantiator(PersistentEntity<?, ?> entity) {

		if (shouldUseReflectionEntityInstantiator(entity)) {
			return ReflectionEntityInstantiator.INSTANCE;
		}

		throw new UnsupportedOperationException("Unsupported operation since CGLIB support has been removed");
	}

	@Alias
	boolean shouldUseReflectionEntityInstantiator(PersistentEntity<?, ?> entity) {
		return false;
	}

	@Delete
	protected EntityInstantiator doCreateEntityInstantiator(PersistentEntity<?, ?> entity) {
		return null;
	}
}

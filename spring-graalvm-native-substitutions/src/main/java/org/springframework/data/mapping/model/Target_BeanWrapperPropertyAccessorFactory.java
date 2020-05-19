package org.springframework.data.mapping.model;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveCglibSupport;

@TargetClass(className = "org.springframework.data.mapping.model.BeanWrapperPropertyAccessorFactory", onlyWith = { OnlyPresent.class, RemoveCglibSupport.class })
public final class Target_BeanWrapperPropertyAccessorFactory implements PersistentPropertyAccessorFactory {

	@Alias
	public static Target_BeanWrapperPropertyAccessorFactory INSTANCE;

	@Alias
	public <T> PersistentPropertyAccessor<T> getPropertyAccessor(PersistentEntity<?, ?> entity, T bean) {
		return null;
	}

	@Alias
	public boolean isSupported(PersistentEntity<?, ?> entity) {
		return false;
	}
}

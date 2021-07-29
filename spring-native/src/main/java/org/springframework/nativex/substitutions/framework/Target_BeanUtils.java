package org.springframework.nativex.substitutions.framework;

import java.lang.reflect.Constructor;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.util.Assert;

// Allows BeanUtils#instantiateClass inlining, see https://github.com/spring-projects-experimental/spring-native/issues/834
@TargetClass(className = "org.springframework.beans.BeanUtils", onlyWith = { OnlyIfPresent.class })
final class Target_BeanUtils {

	@Substitute
	public static <T> T instantiateClass(Class<T> clazz) throws BeanInstantiationException {
		Assert.notNull(clazz, "Class must not be null");
		if (clazz.isInterface()) {
			throw new BeanInstantiationException(clazz, "Specified class is an interface");
		}
		Constructor<T> ctor;
		try {
			ctor = clazz.getDeclaredConstructor();
		} catch (NoSuchMethodException ex) {
			ctor = findPrimaryConstructor(clazz);
			if (ctor == null) {
				throw new BeanInstantiationException(clazz, "No default constructor found", ex);
			}
		}
		return instantiateClass(ctor);
	}

	@Alias
	public static <T> T instantiateClass(Constructor<T> ctor, Object... args) throws BeanInstantiationException {
		return null;
	}

	@Alias
	public static <T> Constructor<T> findPrimaryConstructor(Class<T> clazz) {
		return null;
	}
}

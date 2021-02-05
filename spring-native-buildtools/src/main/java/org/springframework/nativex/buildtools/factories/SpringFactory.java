package org.springframework.nativex.buildtools.factories;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.type.classreading.ClassDescriptor;
import org.springframework.core.type.classreading.TypeDescriptor;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.type.MissingTypeException;

/**
 * An entry in a {@code spring.factories} file.
 * 
 * @author Brian Clozel
 */
public class SpringFactory {

	private static final Log logger = LogFactory.getLog(SpringFactory.class);

	private final ClassDescriptor factoryType;

	private final ClassDescriptor factory;

	private SpringFactory(ClassDescriptor factoryType, ClassDescriptor factory) {
		this.factoryType = factoryType;
		this.factory = factory;
	}

	public static SpringFactory resolve(String factoryTypeName, String factoryName, TypeSystem typeSystem) {
		try {
			ClassDescriptor factoryType = typeSystem.resolveClass(factoryTypeName);
			ClassDescriptor factory = typeSystem.resolveClass(factoryName);
			if (factoryType == null) {
				logger.debug("Could not resolve factory type "+factoryType);
				return null;
			}
			if (factory == null) {
				logger.debug("Could not resolve factory "+factoryName);
				return null;
			}
			return new SpringFactory(factoryType, factory);
		}
		catch (MissingTypeException exc) { // TODO can this still happen with the new type system?
			logger.debug("Could not load SpringFactory: " + exc.getMessage());
			return null;
		}
	}

	public ClassDescriptor getFactoryType() {
		return this.factoryType;
	}

	public ClassDescriptor getFactory() {
		return this.factory;
	}

	@Override
	public String toString() {
		return "SpringFactory{" +
				"factoryType=" + factoryType.getClassName() +
				", factory=" + factory.getClassName() +
				'}';
	}
}

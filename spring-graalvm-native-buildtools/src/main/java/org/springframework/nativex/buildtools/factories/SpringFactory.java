package org.springframework.nativex.buildtools.factories;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.nativex.type.MissingTypeException;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;

/**
 * An entry in a {@code spring.factories} file.
 * 
 * @author Brian Clozel
 */
public class SpringFactory {

	private static final Log logger = LogFactory.getLog(SpringFactory.class);

	private final Type factoryType;

	private final Type factory;

	private SpringFactory(Type factoryType, Type factory) {
		this.factoryType = factoryType;
		this.factory = factory;
	}

	public static SpringFactory resolve(String factoryTypeName, String factoryName, TypeSystem typeSystem) {
		try {
			Type factoryType = typeSystem.resolveDotted(factoryTypeName);
			Type factory = typeSystem.resolveDotted(factoryName);
			return new SpringFactory(factoryType, factory);
		}
		catch (MissingTypeException exc) {
			logger.error("could not load factory", exc);
			logger.error("typesystem classpath: " + typeSystem.getClasspath());
			return null;
		}
	}

	public Type getFactoryType() {
		return this.factoryType;
	}

	public Type getFactory() {
		return this.factory;
	}

	@Override
	public String toString() {
		return "SpringFactory{" +
				"factoryType=" + factoryType.getDottedName() +
				", factory=" + factory.getDottedName() +
				'}';
	}
}

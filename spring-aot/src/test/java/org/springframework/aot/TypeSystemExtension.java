package org.springframework.aot;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.classreading.TypeSystem;

/**
 * JUnit extension that creates a test-level {@link TypeSystem} using the test class classloader
 * and injects it as a test method parameter.
 *
 * @author Brian Clozel
 *
 * @see {@code org.springframework.boot.testsupport.classpath.ModifiedClassPathClassLoader}
 */
public class TypeSystemExtension implements BeforeAllCallback, ParameterResolver {

	private TypeSystem typeSystem;

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		this.typeSystem = TypeSystem.getTypeSystem(new DefaultResourceLoader(extensionContext.getRequiredTestClass().getClassLoader()));
	}


	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		return parameterContext.getParameter().getType().isAssignableFrom(TypeSystem.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		return this.typeSystem;
	}

}

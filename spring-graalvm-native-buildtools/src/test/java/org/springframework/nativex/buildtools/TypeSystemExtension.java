package org.springframework.nativex.buildtools;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import org.springframework.nativex.type.TypeSystem;

/**
 * JUnit extension that creates a test-level {@link TypeSystem} using the test class classloader
 * and injects it as a test method parameter.
 * 
 * @author Brian Clozel
 */
public class TypeSystemExtension implements BeforeAllCallback, ParameterResolver {

	private TypeSystem typeSystem;

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		List<String> classpath = new ArrayList<>();
		URLClassLoader classLoader = (URLClassLoader) extensionContext.getRequiredTestClass().getClassLoader();
		for (URL location : classLoader.getURLs()) {
			String path = location.getPath();
			classpath.add(path);
		}
		this.typeSystem = new TypeSystem(classpath);
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

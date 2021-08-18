package org.springframework.context.bootstrap.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;

import org.springframework.context.bootstrap.generator.bean.descriptor.ProtectedAccessAnalyzer;
import org.springframework.context.bootstrap.generator.reflect.RuntimeReflectionRegistry;

/**
 * Context for components that write code to boostrap the context.
 *
 * @author Stephane Nicoll
 */
public class BootstrapWriterContext {

	static final String BOOTSTRAP_CLASS_NAME = "ContextBootstrapInitializer";

	private final String packageName;

	private final ProtectedAccessAnalyzer protectedAccessAnalyzer;

	private final Map<String, BootstrapClass> bootstrapClasses = new HashMap<>();

	private final RuntimeReflectionRegistry runtimeReflectionRegistry = new RuntimeReflectionRegistry();

	public BootstrapWriterContext(BootstrapClass defaultJavaFile) {
		this.packageName = defaultJavaFile.getClassName().packageName();
		this.protectedAccessAnalyzer = new ProtectedAccessAnalyzer(this.packageName);
		this.bootstrapClasses.put(packageName, defaultJavaFile);
	}

	/**
	 * Return the package name in which the main bootstrap class is located.
	 * @return the default package name
	 */
	public String getPackageName() {
		return this.packageName;
	}

	/**
	 * Return the {@link ProtectedAccessAnalyzer} to use.
	 * @return the protected access analyzer
	 */
	public ProtectedAccessAnalyzer getProtectedAccessAnalyzer() {
		return this.protectedAccessAnalyzer;
	}

	/**
	 * Return a {@link BootstrapClass} for the specified package name. If it does not
	 * exist, it is created
	 * @param packageName the package name to use
	 * @return the bootstrap class
	 */
	public BootstrapClass getBootstrapClass(String packageName) {
		return this.bootstrapClasses.computeIfAbsent(packageName, (p) ->
				BootstrapClass.of(ClassName.get(packageName, BOOTSTRAP_CLASS_NAME),
						(type) -> type.addModifiers(Modifier.PUBLIC, Modifier.FINAL)));
	}

	/**
	 * Specify if a {@link BootstrapClass} for the specified package name is registered.
	 * @param packageName the package name to use
	 * @return {@code true} if the class is registered for that package
	 */
	public boolean hasBootstrapClass(String packageName) {
		return this.bootstrapClasses.containsKey(packageName);
	}

	/**
	 * Return the list of {@link JavaFile} of known bootstrap classes.
	 * @return the java files of bootstrap classes in this instance
	 */
	public List<JavaFile> toJavaFiles() {
		return this.bootstrapClasses.values().stream().map(BootstrapClass::toJavaFile).collect(Collectors.toList());
	}

	/**
	 * Return a {@link RuntimeReflectionRegistry} for recording the need of runtime reflection
	 * at runtime for the generated code.
	 * @return the reflection registry
	 */
	public RuntimeReflectionRegistry getRuntimeReflectionRegistry() {
		return this.runtimeReflectionRegistry;
	}

}

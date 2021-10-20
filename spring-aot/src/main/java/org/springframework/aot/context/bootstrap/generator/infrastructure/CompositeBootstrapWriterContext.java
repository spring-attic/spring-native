package org.springframework.aot.context.bootstrap.generator.infrastructure;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.squareup.javapoet.JavaFile;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;

/**
 * A composite that handles multiple {@link BootstrapWriterContext} with a unique
 * {@link NativeConfigurationRegistry}. Used when multiple contexts have to be written,
 * yet sharing a single, unified native configuration.
 *
 * @author Stephane Nicoll
 * @see BootstrapWriterContext
 */
public class CompositeBootstrapWriterContext {

	private final String packageName;

	private final NativeConfigurationRegistry nativeConfigurationRegistry;

	private final Map<String, BootstrapWriterContext> bootstrapWriterContexts;

	/**
	 * Create an instance for the specified main package name.
	 * @param packageName the main package name
	 */
	public CompositeBootstrapWriterContext(String packageName) {
		this.packageName = packageName;
		this.nativeConfigurationRegistry = new NativeConfigurationRegistry();
		this.bootstrapWriterContexts = new LinkedHashMap<>();
	}

	/**
	 * Return a {@link BootstrapWriterContext} for the specified {@code className}.
	 * @param className the class name of a context.
	 * @return a {@link BootstrapWriterContext} for the specified class name
	 * @see BootstrapWriterContext#bootstrapClassFactory(String, String)
	 * @throws IllegalArgumentException if a context with that class name already exists
	 */
	public BootstrapWriterContext createBootstrapWriterContext(String className) {
		return createBootstrapWriterContext(className, BootstrapWriterContext
				.bootstrapClassFactory(this.packageName, className));
	}

	/**
	 * Return a {@link BootstrapWriterContext} for the specified {@code id}, using the
	 * specified factory to create a {@link BootstrapClass} per requested package name.
	 * @param id the identifier to use
	 * @param bootstrapClassFactory the factory to use to create a {@link BootstrapClass}
	 * based on a package name.
	 * @return a {@link BootstrapWriterContext} for the specified id
	 * @throws IllegalArgumentException if a context with that class name already exists
	 */
	public BootstrapWriterContext createBootstrapWriterContext(String id, Function<String, BootstrapClass> bootstrapClassFactory) {
		if (this.bootstrapWriterContexts.containsKey(id)) {
			throw new IllegalArgumentException("context with id '" + id + "' already exists");
		}
		BootstrapWriterContext writerContext = new BootstrapWriterContext(
				this.packageName, bootstrapClassFactory, this.nativeConfigurationRegistry);
		this.bootstrapWriterContexts.put(id, writerContext);
		return writerContext;
	}

	/**
	 * Return the list of {@link JavaFile} of known classes that have been contributed to
	 * this context.
	 * @return the java files of this instance
	 */
	public List<JavaFile> toJavaFiles() {
		return this.bootstrapWriterContexts.values().stream()
				.flatMap((context) -> context.toJavaFiles().stream()).collect(Collectors.toList());
	}

	/**
	 * Return a {@link NativeConfigurationRegistry} for recording the necessary native
	 * configuration for this context
	 * @return the native configuration registry
	 */
	public NativeConfigurationRegistry getNativeConfigurationRegistry() {
		return this.nativeConfigurationRegistry;
	}

}

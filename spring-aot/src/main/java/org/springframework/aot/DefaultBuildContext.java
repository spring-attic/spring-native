package org.springframework.aot;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;

/**
 * Default implementation for the {@link BuildContext}
 */
class DefaultBuildContext implements BuildContext {

	private final TypeSystem typeSystem;

	private final List<String> classpath;

	private final List<SourceFile> sourceFiles = new ArrayList<>();

	private final List<ResourceFile> resourceFiles = new ArrayList<>();

	private final ReflectionDescriptor reflectionDescriptor = new ReflectionDescriptor();

	private final ProxiesDescriptor proxiesDescriptor = new ProxiesDescriptor();

	private final ResourcesDescriptor resourcesDescriptor = new ResourcesDescriptor();

	DefaultBuildContext(List<String> classpath) {
		this.classpath = classpath;
		this.typeSystem = TypeSystem.getTypeSystem(new DefaultResourceLoader(getBootstrapClassLoader(classpath)));
	}

	@Override
	public TypeSystem getTypeSystem() {
		return this.typeSystem;
	}

	@Override
	public List<String> getClasspath() {
		return this.classpath;
	}

	@Override
	public void addSourceFiles(SourceFile... sourceFiles) {
		this.sourceFiles.addAll(Arrays.asList(sourceFiles));
	}

	@Override
	public void addResources(ResourceFile... resourceFiles) {
		this.resourceFiles.addAll(Arrays.asList(resourceFiles));
	}

	@Override
	public void describeReflection(Consumer<ReflectionDescriptor> consumer) {
		consumer.accept(this.reflectionDescriptor);
	}

	@Override
	public void describeProxies(Consumer<ProxiesDescriptor> consumer) {
		consumer.accept(this.proxiesDescriptor);
	}

	@Override
	public void describeResources(Consumer<ResourcesDescriptor> consumer) {
		consumer.accept(this.resourcesDescriptor);
	}

	List<SourceFile> getSourceFiles() {
		return this.sourceFiles;
	}

	List<ResourceFile> getResourceFiles() {
		return this.resourceFiles;
	}

	public ReflectionDescriptor getReflectionDescriptor() {
		return this.reflectionDescriptor;
	}

	public ProxiesDescriptor getProxiesDescriptor() {
		return this.proxiesDescriptor;
	}

	public ResourcesDescriptor getResourcesDescriptor() {
		return this.resourcesDescriptor;
	}

	private URLClassLoader getBootstrapClassLoader(List<String> classpath) {
		try {
			List<URL> urls = new ArrayList<>();
			List<URI> uris = classpath.stream().map(File::new).map(File::toURI).collect(Collectors.toList());
			for (URI uri : uris) {
				urls.add(uri.toURL());
			}
			return new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader().getParent());
		}
		catch (Exception ex) {
			throw new CodeGenerationException("Unable to build classpath", ex);
		}
	}
}

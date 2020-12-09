package org.springframework.nativex.buildtools;

import java.util.function.Consumer;

import org.springframework.nativex.domain.proxies.ProxiesDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;
import org.springframework.nativex.type.TypeSystem;

/**
 * Provide build context information.
 *
 * @author Brian Clozel
 */
public interface BuildContext {

	/**
	 * Return the "compile+runtime" {@link ClassLoader} for the application.
	 * @deprecated in favor of the {@link #getTypeSystem() TypeSystem}.
	 */
	@Deprecated
	ClassLoader getClassLoader();

	/**
	 * Return the {@link TypeSystem} based on the "compile+runtime" application classpath.
	 */
	TypeSystem getTypeSystem();

	/**
	 * Contribute source files to the application.
	 * <p>This additional source code will be compiled and packaged with the application by a build plugin.
	 */
	void addSourceFiles(SourceFile... sourceFile);

	/**
	 * Contribute resource files to the application.
	 * <p>This additional packaged with the application by a build plugin.
	 */
	void addResources(ResourceFile... resourceFile);

	/**
	 * Contribute reflection information to the application.
	 */
	void describeReflection(Consumer<ReflectionDescriptor> consumer);

	/**
	 * Contribute proxies information to the application.
	 */
	void describeProxies(Consumer<ProxiesDescriptor> consumer);

	/**
	 * Contribute resources information to the application.
	 */
	void describeResources(Consumer<ResourcesDescriptor> consumer);

}

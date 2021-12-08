/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aot.context.bootstrap.generator.infrastructure;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;

import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Infrastructure used by context writers. Provide a main {@link BootstrapClass} as well
 * as a way to contribute additional {@link BootstrapClass classes} if privileged access
 * is required.
 * <p>
 * A component may decide to fork a context at any time if they need to register a
 * separate context, yet keeping a unique native configuration registry and view on the
 * generated bootstrap classes.
 *
 * @author Stephane Nicoll
 */
public interface BootstrapWriterContext {

	/**
	 * Return a {@link NativeConfigurationRegistry} for recording the necessary native
	 * configuration.
	 * @return the native configuration registry
	 */
	NativeConfigurationRegistry getNativeConfigurationRegistry();

	/**
	 * Return the {@link ProtectedAccessAnalyzer} to use.
	 * @return the protected access analyzer
	 */
	ProtectedAccessAnalyzer getProtectedAccessAnalyzer();

	/**
	 * Return a {@link BootstrapClass} for the specified package name. If it does not
	 * exist, it is created.
	 * @param packageName the package name to use
	 * @return the bootstrap class
	 */
	BootstrapClass getBootstrapClass(String packageName);

	/**
	 * Return the main {@link BootstrapClass}.
	 * @return the bootstrap class for the target package
	 */
	BootstrapClass getMainBootstrapClass();

	/**
	 * Fork a new {@link BootstrapWriterContext} for the specified {@code ClassName} using
	 * a factory that creates a {@link BootstrapClass} for the main context as an
	 * {@link ApplicationContextInitializer} and the package protected boostrap classes
	 * as empty {@code public final} types. The specified {@link ClassName} is used as
	 * an identifier for the forked context.
	 * @param className the root class name for a context.
	 * @return a {@link BootstrapWriterContext} for the specified class name
	 * @see BootstrapWriterContext#bootstrapClassFactory(String, String)
	 * @throws IllegalArgumentException if a context with that class name already exists
	 */
	BootstrapWriterContext fork(ClassName className);

	/**
	 * Fork a new {@link BootstrapWriterContext} for the specified {@code id}, using the
	 * specified factory to create a {@link BootstrapClass} per requested package name.
	 * @param id the identifier to use
	 * @param bootstrapClassFactory the factory to use to create a {@link BootstrapClass}
	 * based on a package name.
	 * @return a {@link BootstrapWriterContext} for the specified id
	 * @throws IllegalArgumentException if a context with that class name already exists
	 */
	BootstrapWriterContext fork(String id, Function<String, BootstrapClass> bootstrapClassFactory);

	/**
	 * Return a function that creates a {@link BootstrapClass} for the main context as an
	 * {@link ApplicationContextInitializer} and the package protected boostrap classes
	 * as empty {@code public final} types.
	 * @param mainPackageName the package name for the main context
	 * @param className the class name to use
	 * @return a function to create boostrap classes
	 */
	static Function<String, BootstrapClass> bootstrapClassFactory(String mainPackageName, String className) {
		return (targetPackageName) -> {
			if (targetPackageName.equals(mainPackageName)) {
				ParameterizedTypeName typeName = ParameterizedTypeName.get(ApplicationContextInitializer.class,
						GenericApplicationContext.class);
				return BootstrapClass.of(ClassName.get(targetPackageName, className), (type) ->
						type.addSuperinterface(typeName).addModifiers(Modifier.PUBLIC));
			}
			else {
				return BootstrapClass.of(ClassName.get(targetPackageName, className),
						(type) -> type.addModifiers(Modifier.PUBLIC, Modifier.FINAL));
			}
		};
	}

}

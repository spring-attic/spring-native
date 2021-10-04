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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * Represent a class that provide registration for beans that are not available outside of
 * a particular package.
 *
 * @author Stephane Nicoll
 */
public class BootstrapClass {

	private static final String BOOTSTRAP_CLASS_NAME = "ContextBootstrapInitializer";

	private final ClassName className;

	private final TypeSpec.Builder type;

	private final List<MethodSpec> methods;

	BootstrapClass(ClassName className, Consumer<TypeSpec.Builder> type) {
		this.className = className;
		this.type = TypeSpec.classBuilder(className);
		type.accept(this.type);
		this.methods = new ArrayList<>();
	}

	/**
	 * Create an instance for the specified package.
	 * @param packageName the package name
	 * @param type a callback to customize the type, i.e. to change default modifiers
	 * @return a new {@link BootstrapClass}
	 */
	public static BootstrapClass of(String packageName, Consumer<TypeSpec.Builder> type) {
		return new BootstrapClass(ClassName.get(packageName, BOOTSTRAP_CLASS_NAME), type);
	}

	/**
	 * Create an instance for the specified package, as a {@code public} type.
	 * @param packageName the package name
	 * @return a new {@link BootstrapClass}
	 */
	public static BootstrapClass of(String packageName) {
		return of(packageName, (type) -> type.addModifiers(Modifier.PUBLIC));
	}

	/**
	 * Return the {@link ClassName} of this instance.
	 * @return the class name
	 */
	public ClassName getClassName() {
		return this.className;
	}

	/**
	 * Add the specified {@link MethodSpec method}.
	 * @param method the method to add
	 */
	public void addMethod(MethodSpec method) {
		this.methods.add(method);
	}

	/**
	 * Return a {@link JavaFile} with the state of this instance
	 * @return a java file
	 */
	public JavaFile toJavaFile() {
		return JavaFile.builder(this.className.packageName(),
				this.type.addMethods(this.methods).build()).build();
	}

}

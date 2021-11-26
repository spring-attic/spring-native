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
 * Represent a class that provide registration for beans that are not available outside a
 * particular package.
 *
 * @author Stephane Nicoll
 */
public class BootstrapClass {

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
	 * Create an instance for the specified {@link ClassName}, customizing the type with
	 * the specified {@link Consumer consumer callback}.
	 * @param className the class name
	 * @param type a callback to customize the type, i.e. to change default modifiers
	 * @return a new {@link BootstrapClass}
	 */
	public static BootstrapClass of(ClassName className, Consumer<TypeSpec.Builder> type) {
		return new BootstrapClass(className, type);
	}

	/**
	 * Create an instance for the specified {@link  ClassName}, as a {@code public} type.
	 * @param className the class name
	 * @return a new {@link BootstrapClass}
	 */
	public static BootstrapClass of(ClassName className) {
		return of(className, (type) -> type.addModifiers(Modifier.PUBLIC));
	}

	/**
	 * Return the {@link ClassName} of this instance.
	 * @return the class name
	 */
	public ClassName getClassName() {
		return this.className;
	}

	/**
	 * Customize the type of this instance.
	 * @param type the consumer of the type builder
	 * @return this for method chaining
	 */
	public BootstrapClass customizeType(Consumer<TypeSpec.Builder> type) {
		type.accept(this.type);
		return this;
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

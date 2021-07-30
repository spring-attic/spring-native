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

package org.springframework.context.bootstrap.generator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
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

	private final String packageName;

	private final ClassName className;

	private final TypeSpec.Builder type;

	private final Map<String, MethodSpec> methods;

	BootstrapClass(String packageName, String name, Consumer<TypeSpec.Builder> type) {
		this.packageName = packageName;
		this.className = ClassName.get(packageName, name);
		this.type = TypeSpec.classBuilder(className);
		type.accept(this.type);
		this.methods = new LinkedHashMap<>();
	}

	BootstrapClass(String packageName, String name) {
		this(packageName, name, (type) -> {
		});
	}

	public ClassName getClassName() {
		return this.className;
	}

	public boolean hasMethod(String name) {
		return this.methods.containsKey(name);
	}

	public void addMethod(MethodSpec method) {
		this.methods.put(method.name, method);
	}

	public void addMethod(MethodSpec.Builder method, Consumer<Builder> code) {
		CodeBlock.Builder body = CodeBlock.builder();
		code.accept(body);
		method.addCode(body.build());
		addMethod(method.build());
	}

	public JavaFile build() {
		return JavaFile.builder(this.packageName, this.type.addMethods(this.methods.values()).build()).build();
	}

}

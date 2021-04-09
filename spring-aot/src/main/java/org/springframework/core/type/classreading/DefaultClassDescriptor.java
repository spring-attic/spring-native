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

package org.springframework.core.type.classreading;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.asm.Opcodes;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * Default implementation for {@link ClassDescriptor}
 * @author Brian Clozel
 */
public class DefaultClassDescriptor implements ClassDescriptor {

	private static final char CLASS_SEPARATOR = '.';

	private static final char INNER_CLASS_SEPARATOR = '$';

	private final TypeSystem typeSystem;

	private final String className;

	private final int access;

	@Nullable
	private final String superClassName;

	@Nullable
	private final String enclosingClassName;

	private final Set<String> interfaceNames;

	private final Set<String> memberClassNames;

	private final  List<MethodDescriptor> methods;

	private final MergedAnnotations annotations;


	public DefaultClassDescriptor(TypeSystem typeSystem, String className, int access, @Nullable String superClassName,
			@Nullable String enclosingClassName, Set<String> interfaceNames, Set<String> memberClassNames,
			List<MethodDescriptor> methods, MergedAnnotations annotations) {
		this.typeSystem = typeSystem;
		this.className = className;
		this.access = access;
		this.superClassName = superClassName;
		this.enclosingClassName = enclosingClassName;
		this.interfaceNames = interfaceNames;
		this.memberClassNames = memberClassNames;
		this.methods = methods;
		this.annotations = annotations;
	}

	@Override
	public String getPackageName() {
		return ClassUtils.getPackageName(this.className);
	}

	@Override
	public String getClassName() {
		return this.className;
	}

	@Override
	public String getCanonicalClassName() {
		return this.className.replace(INNER_CLASS_SEPARATOR, CLASS_SEPARATOR);
	}

	@Override
	public String getShortName() {
		return ClassUtils.getShortName(this.className);
	}

	@Override
	public boolean isInterface() {
		return (this.access & Opcodes.ACC_INTERFACE) != 0;
	}

	@Override
	public boolean isAnnotation() {
		return (this.access & Opcodes.ACC_ANNOTATION) != 0;
	}

	@Override
	public boolean isAbstract() {
		return (this.access & Opcodes.ACC_ABSTRACT) != 0;
	}

	@Override
	public boolean isPublic() {
		return (this.access & Opcodes.ACC_PUBLIC) != 0;
	}

	@Override
	public ClassDescriptor getSuperClass() {
		if (this.superClassName != null) {
			return this.typeSystem.resolveClass(this.superClassName);
		}
		return null;
	}

	@Override
	public Stream<ClassDescriptor> getInterfaces() {
		return this.interfaceNames.stream().map(this.typeSystem::resolveClass);
	}

	@Override
	public ClassDescriptor getEnclosingClass() {
		if (this.enclosingClassName != null) {
			return this.typeSystem.resolveClass(this.enclosingClassName);
		}
		return null;
	}

	@Override
	public Stream<ClassDescriptor> getMemberClasses() {
		return this.memberClassNames.stream().map(this.typeSystem::resolveClass);
	}

	@Override
	public Stream<MethodDescriptor> getMethods() {
		return this.methods.stream();
	}

	@Override
	public MergedAnnotations getAnnotations() {
		return this.annotations;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DefaultClassDescriptor that = (DefaultClassDescriptor) o;
		return className.equals(that.className);
	}

	@Override
	public int hashCode() {
		return Objects.hash(className);
	}
}

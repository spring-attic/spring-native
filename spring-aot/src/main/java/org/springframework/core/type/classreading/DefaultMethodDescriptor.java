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

import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.core.annotation.MergedAnnotations;

/**
 * Default implementation for {@link MethodDescriptor}
 * @author Brian Clozel
 */
public class DefaultMethodDescriptor implements MethodDescriptor {

	private final TypeSystem typeSystem;

	private final String methodName;

	private final int access;

	private final String declaringClassName;

	private final String descriptor;

	private final MergedAnnotations annotations;

	public DefaultMethodDescriptor(TypeSystem typeSystem, String methodName, int access, String declaringClassName,
			String descriptor, MergedAnnotations annotations) {
		this.typeSystem = typeSystem;
		this.methodName = methodName;
		this.access = access;
		this.declaringClassName = declaringClassName;
		this.descriptor = descriptor;
		this.annotations = annotations;
	}

	@Override
	public String getMethodName() {
		return this.methodName;
	}

	@Override
	public ClassDescriptor getDeclaringClass() {
		return this.typeSystem.resolveClass(this.declaringClassName);
	}

	@Override
	public TypeDescriptor getReturnType() {
		Type returnType = Type.getReturnType(this.descriptor);
		return this.typeSystem.resolve(TypeName.fromTypeSignature(returnType.toString()).getClassName());
	}

	@Override
	public boolean isAbstract() {
		return (this.access & Opcodes.ACC_ABSTRACT) != 0;
	}

	@Override
	public boolean isStatic() {
		return (this.access & Opcodes.ACC_STATIC) != 0;
	}

	@Override
	public boolean isFinal() {
		return (this.access & Opcodes.ACC_FINAL) != 0;
	}

	@Override
	public boolean isOverridable() {
		return !isStatic() && !isFinal() && !isPrivate();
	}

	@Override
	public boolean isPublic() {
		return (this.access & Opcodes.ACC_PUBLIC) != 0;
	}

	public boolean isPrivate() {
		return (this.access & Opcodes.ACC_PRIVATE) != 0;
	}

	@Override
	public boolean isConstructor() {
		return TypeName.from(this.declaringClassName).getConstructorName().equals(this.methodName);
	}

	@Override
	public int getArgumentsCount() {
		return Type.getMethodType(this.descriptor).getArgumentTypes().length;
	}

	@Override
	public MergedAnnotations getAnnotations() {
		return this.annotations;
	}
}

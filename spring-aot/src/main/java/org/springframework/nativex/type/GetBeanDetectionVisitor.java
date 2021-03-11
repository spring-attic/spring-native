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

package org.springframework.nativex.type;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Class visitor that will check if a type is making <tt>org.springframework.beans.factory.getBean(...)</tt>
 * calls. 
 * 
 * @author Andy Clement
 */
class GetBeanDetectionVisitor extends ClassVisitor {

	private List<String> methodsUsingGetBeanCalls = null;
	
	public static void main(String[] args) throws Exception {
		System.out.println(run(new FileInputStream(args[0])));
	}

	/**
	 * @param inputStream input stream for a class file
	 * @return null if there are no isPresent() checks otherwise a list of types
	 *         isPresent() checks are being made upon (may be empty if isPresent()
	 *         checks are made but the target cannot be determined)
	 */
	public static List<String> run(InputStream inputStream) {
		try {
			GetBeanDetectionVisitor node = new GetBeanDetectionVisitor(Opcodes.ASM9);
			ClassReader reader = new ClassReader(inputStream);
			reader.accept(node, ClassReader.SKIP_DEBUG);
			return node.methodsUsingGetBeanCalls;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private GetBeanDetectionVisitor(int api) {
		super(api);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		return new GetBeanFindingMethodVisitor(name, this.api);
	}

	class GetBeanFindingMethodVisitor extends MethodVisitor {

		private String methodName;

		public GetBeanFindingMethodVisitor(String name, int api) {
			super(api);
			this.methodName = name;
		}

		// Example:
		// 3: invokeinterface #43,  2 // InterfaceMethod org/springframework/beans/factory/BeanFactory.getBean:(Ljava/lang/String;)Ljava/lang/Object;
		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean itface) {
			if (opcode == Opcodes.INVOKEINTERFACE && owner.equals("org/springframework/beans/factory/BeanFactory")
					&& name.equals("getBean")) {
				if (methodsUsingGetBeanCalls == null) {
					methodsUsingGetBeanCalls = new ArrayList<>();
				}
				methodsUsingGetBeanCalls.add(methodName);
			} else {
				super.visitMethodInsn(opcode, owner, name, descriptor, itface);
			}
		}

	}
}
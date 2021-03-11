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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Class visitor that will check if a type is making isPresent() calls from the
 * static initializer. If calls are being made it will try to determine the
 * parameter to the isPresent() call.
 * 
 * @author Andy Clement
 */
class IsPresentDetectionVisitor extends ClassVisitor {

	private String classname;

	private boolean containsIsPresentChecksInStaticInitializer = false;

	private List<String> typesCheckedInIsPresentCalls = new ArrayList<>();

	/**
	 * Visit the class supplied in the input stream and determine if isPresent()
	 * calls are made from the static initializer. If they are try to determine what
	 * the isPresent() checks are made against.
	 * 
	 * @param inputStream input stream for a class file
	 * @return null if there are no isPresent() checks otherwise a list of types
	 *         isPresent() checks are being made upon (may be empty if isPresent()
	 *         checks are made but the target cannot be determined)
	 */
	public static List<String> run(InputStream inputStream) {
		try {
			IsPresentDetectionVisitor node = new IsPresentDetectionVisitor(Opcodes.ASM9);
			ClassReader reader = new ClassReader(inputStream);
			reader.accept(node, ClassReader.SKIP_DEBUG);
			return (node.containsIsPresentChecksInStaticInitializer ? node.typesCheckedInIsPresentCalls : null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private IsPresentDetectionVisitor(int api) {
		super(api);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this.classname = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		if (name.equals("<clinit>")) {
			return new ClinitVisitor(this.api);
		} else {
			return super.visitMethod(access, name, descriptor, signature, exceptions);
		}
	}

	class ClinitVisitor extends MethodVisitor {

		private int state = 0;
		private String mostRecentlyLoadedString;

		public ClinitVisitor(int api) {
			super(api);
		}

		// Typical pattern:
		// 6: ldc           #88                 // String javax.annotation.Resource
 		// 8: aload_0
        // 9: invokestatic  #89                 // Method org/springframework/util/ClassUtils.isPresent:(Ljava/lang/String;Ljava/lang/ClassLoader;)Z

		@Override
		public void visitLdcInsn(Object value) {
			if (value instanceof String) {
				mostRecentlyLoadedString = (String) value;
				state = 1;
			}
			super.visitLdcInsn(value);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean itface) {
			if (opcode == Opcodes.INVOKESTATIC && owner.equals("org/springframework/util/ClassUtils")
					&& name.equals("isPresent") && descriptor.equals("(Ljava/lang/String;Ljava/lang/ClassLoader;)Z")) {
				containsIsPresentChecksInStaticInitializer = true;
				if (state == 1) {
					typesCheckedInIsPresentCalls.add(mostRecentlyLoadedString);
					state = 0;
				}
			} else {
				super.visitMethodInsn(opcode, owner, name, descriptor, itface);
			}
		}

	}
}
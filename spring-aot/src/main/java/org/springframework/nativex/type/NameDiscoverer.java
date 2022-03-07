/*
 * Copyright 2019-2022 the original author or authors.
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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.SpringAsmInfo;

/**
 * @author Andy Clement
 */
public class NameDiscoverer {

	public static String getClassName(byte[] bytes) {
		ClassReader reader = new ClassReader(bytes);
		NameDiscoveringVisitor nd = new NameDiscoveringVisitor();
		reader.accept(nd, ClassReader.SKIP_DEBUG);
		return nd.name;
	}

	public static String getClassName(Path path) {
		try {
			byte[] bytes = Files.readAllBytes(path);
			return getClassName(bytes);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static class NameDiscoveringVisitor extends ClassVisitor {

		public String name;

		public NameDiscoveringVisitor() {
			super(SpringAsmInfo.ASM_VERSION);
		}
		
		@Override
		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			this.name = name;
		}
	}

	public static String getClassName(URL resource) {
		try {
			return getClassName(Path.of(resource.toURI()));
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

}
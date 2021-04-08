/*
 * Copyright 2021 the original author or authors.
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

package org.springframework.nativex;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.nativex.support.Utils;
import org.springframework.nativex.type.ConstantPoolScanner;
import org.springframework.nativex.type.TypeSystem;

public class UtilTests {

	static TypeSystem typeSystem;

	@BeforeAll
	public static void setup() throws Exception {
		File file = new File("./target/test-classes");
		typeSystem = new TypeSystem(Collections.singletonList(file.toString()));
	}

	@Test
	public void superclass() {
		ConstantPoolScanner s = Utils.scanClass(typeSystem.find(Foo.class.getName().replace(".", "/")));
		assertThat(s.getSuperclassname()).isEqualTo("java/lang/Object");
		String[] ifs = s.getInterfacenames();
		assertThat(ifs.length).isEqualTo(0);
	}
	
	static class Foo extends Object {}
	
	@Test
	public void interfaces() {
		ConstantPoolScanner s = Utils.scanClass(typeSystem.find(Bar.class.getName().replace(".", "/")));
		assertThat(s.getSuperclassname()).isEqualTo("java/lang/Object");
		String[] ifs = s.getInterfacenames();
		assertThat(ifs.length).isEqualTo(1);
		assertThat(ifs[0]).isEqualTo("java/io/Serializable");
	}

	@SuppressWarnings("serial")
	static class Bar implements Serializable {}

}

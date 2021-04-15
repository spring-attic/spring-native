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

package org.springframework.nativex;

import java.io.File;
import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeSystemTests {

	private static TypeSystem typeSystem;

	@BeforeAll
	public static void setup() throws Exception {
		File file = new File("./target/classes");
		// System.out.println(file.getCanonicalPath());
		typeSystem = new TypeSystem(Collections.singletonList(file.toString()));
	}

	@Test
	public void test() throws Exception {
		Type t = typeSystem.resolveName("java.lang.String");
		assertNotNull(t);
	}
	
	@Test
	public void badpath() throws Exception {
		TypeSystem ts = TypeSystem.get(Collections.singletonList("madeup"));
		ts.findTypesAnnotated(Type.AtConfiguration, false);
	}

	@Test
	public void testArray() throws Exception {
		Type s = typeSystem.resolveName("java.lang.String");
		assertEquals("Ljava/lang/String;", s.getDescriptor());
		Type t = typeSystem.resolveName("java.lang.String[]");
		assertNotNull(t);
		assertEquals(1,t.getDimensions());
		assertTrue(t.isArray());
		assertEquals("[Ljava/lang/String;",t.getDescriptor());
		assertEquals("java/lang/String[]",t.getName());
		assertEquals("java.lang.String[]",t.getDottedName());
	}

}

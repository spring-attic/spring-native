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

package org.springframework.aot.context.bootstrap.generator.infrastructure.nativex;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NativeConfigurationUtils}.
 *
 * @author Andy Clement
 */
class NativeConfigurationUtilsTests {

	@Test
	public void typesInSignature() throws NoSuchMethodException, SecurityException, NoSuchFieldException {
		Set<Class<?>> collected = NativeConfigurationUtils.collectTypesInSignature(this.getClass().getDeclaredMethod("one"));
		assertThat(collected).containsOnly(Foo.class);
		collected = NativeConfigurationUtils.collectTypesInSignature(this.getClass().getDeclaredMethod("two", Foo.class));
		assertThat(collected).containsOnly(Foo.class);
		collected = NativeConfigurationUtils.collectTypesInSignature(this.getClass().getDeclaredMethod("three", List.class));
		assertThat(collected).containsOnly(List.class, Foo.class);
		collected = NativeConfigurationUtils.collectTypesInSignature(this.getClass().getDeclaredMethod("four", Integer.TYPE, List.class, Map.class));
		assertThat(collected).containsOnly(Map.class, String.class, List.class, Foo.class, Bar.class, Integer.class);
		collected = NativeConfigurationUtils.collectTypesInSignature(Boo.class.getDeclaredField("foos"));
		assertThat(collected).containsOnly(List.class,Foo.class);
	}
	
	static class Foo {
		
	}
	
	static class Bar {
		
	}

	public Foo one() {
		return null;
	}
	
	public void two(Foo foo) {
	}

	public void three(List<Foo> foo) {
	}
	
	public Map<String,List<Foo>> four(int i, List<Integer> li, Map<Bar,Foo> map) {
		return null;
	}
	
	static class Boo {
		List<Foo> foos;
	}

}

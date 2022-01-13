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

package org.springframework.data;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.core.ResolvableType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ParameterizedTypeMapper}.
 *
 * @author Stephane Nicoll
 */
class ParameterizedTypeMapperTests {

	@Test
	void mapGenericsWithInvertedGenericParameters() {
		ParameterizedTypeMapper mapper = ParameterizedTypeMapper.of(CustomMapping.class, Mapping.class);
		assertThat(mapper.mapGenericTypes(Integer.class, String.class)).satisfies(ofTypes(String.class, Integer.class));
	}

	@Test
	void mapGenericsWithDoubleInvertedGenericParameters() {
		ParameterizedTypeMapper mapper = ParameterizedTypeMapper.of(AnotherCustomMapping.class, Mapping.class);
		assertThat(mapper.mapGenericTypes(Integer.class, String.class)).satisfies(ofTypes(Integer.class, String.class));
	}

	@Test
	void mapGenericsWithFirstResolvedParameter() {
		ParameterizedTypeMapper mapper = ParameterizedTypeMapper.of(StringInMapping.class, Mapping.class);
		assertThat(mapper.mapGenericTypes(Integer.class, String.class)).satisfies(ofTypes(String.class));
	}

	@Test
	void mapGenericsWithSecondResolvedParameter() {
		ParameterizedTypeMapper mapper = ParameterizedTypeMapper.of(StringOutMapping.class, Mapping.class);
		assertThat(mapper.mapGenericTypes(Integer.class, String.class)).satisfies(ofTypes(Integer.class));
	}


	@SuppressWarnings("rawtypes")
	private Consumer<? super ResolvableType[]> ofTypes(Class<?>... classes) {
		return (types) -> assertThat(types).extracting((type) -> (Class) type.toClass())
				.containsExactly(classes);
	}


	static abstract class Mapping<S, R> {

		protected R map(S source) {
			return null;
		}
	}

	static class CustomMapping<B, A> extends Mapping<A, B> {

	}

	static class AnotherCustomMapping<X, Y> extends CustomMapping<Y, X> {

	}

	static class StringInMapping<X> extends Mapping<String, X> {

	}

	static class StringOutMapping<X> extends Mapping<X, String> {

	}

}

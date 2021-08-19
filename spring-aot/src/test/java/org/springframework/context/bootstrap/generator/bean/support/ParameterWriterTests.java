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

package org.springframework.context.bootstrap.generator.bean.support;

import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.context.bootstrap.generator.test.CodeSnippet;
import org.springframework.core.ResolvableType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ParameterWriter}.
 *
 * @author Stephane Nicoll
 */
class ParameterWriterTests {

	private final ParameterWriter writer = new ParameterWriter();

	@Test
	void writeCharArray() {
		char[] value = new char[] { 'v', 'a', 'l', 'u', 'e' };
		assertThat(write(value, ResolvableType.forArrayComponent(ResolvableType.forClass(char.class))))
				.isEqualTo("new char[] { 'v', 'a', 'l', 'u', 'e' }");
	}

	@Test
	void writeStringArray() {
		String[] value = new String[] { "a", "test" };
		assertThat(write(value, ResolvableType.forArrayComponent(ResolvableType.forClass(String.class))))
				.isEqualTo("new String[] { \"a\", \"test\" }");
	}

	@Test
	void writeString() {
		assertThat(write("test", ResolvableType.forClass(String.class))).isEqualTo("\"test\"");
	}

	@Test
	void writeCharEscapeBackslash() {
		assertThat(write('\\', ResolvableType.forType(char.class))).isEqualTo("'\\\\'");
	}

	@ParameterizedTest
	@MethodSource("primitiveValues")
	void writePrimitiveValue(Object value, String parameter) {
		assertThat(write(value, ResolvableType.forClass(value.getClass()))).isEqualTo(parameter);
	}

	private static Stream<Arguments> primitiveValues() {
		return Stream.of(Arguments.of((short) 0, "0"), Arguments.of((1), "1"), Arguments.of(2L, "2"),
				Arguments.of(2.5d, "2.5"), Arguments.of(2.7f, "2.7"), Arguments.of('c', "'c'"),
				Arguments.of((byte) 1, "1"), Arguments.of(true, "true"));
	}

	@Test
	void writeEnum() {
		assertThat(write(ChronoUnit.DAYS, ResolvableType.forClass(ChronoUnit.class)))
				.isEqualTo("ChronoUnit.DAYS").hasImport(ChronoUnit.class);
	}

	@Test
	void writeClass() {
		assertThat(write(Integer.class, ResolvableType.forClass(Class.class))).isEqualTo("Integer.class");
	}


	private CodeSnippet write(Object value, ResolvableType resolvableType) {
		return CodeSnippet.of(this.writer.writeParameterValue(value, resolvableType));
	}

}

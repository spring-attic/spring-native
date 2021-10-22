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

package org.springframework.aot.context.bootstrap.generator.bean.support;

import com.squareup.javapoet.CodeBlock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link MultiCodeBlock}.
 *
 * @author Stephane Nicoll
 */
class MultiCodeBlockTests {

	@Test
	void joinWithNoElement() {
		MultiCodeBlock multi = new MultiCodeBlock();
		assertThat(multi.join(", ").isEmpty()).isTrue();
	}

	@Test
	void joinWithEmptyElement() {
		MultiCodeBlock multi = new MultiCodeBlock();
		assertThatIllegalArgumentException().isThrownBy(() -> multi.add(CodeBlock.builder().build()));
	}

	@Test
	void joinWithSingleElement() {
		MultiCodeBlock multi = new MultiCodeBlock();
		multi.add(CodeBlock.of("$S", "Hello"));
		assertThat(multi.join(", ")).hasToString("\"Hello\"");
	}

	@Test
	void joinWithSeveralElement() {
		MultiCodeBlock multi = new MultiCodeBlock();
		multi.add(CodeBlock.of("$S", "Hello"));
		multi.add((code) -> code.add("42"));
		multi.add((code) -> code.add("null"));
		assertThat(multi.join(", ")).hasToString("\"Hello\", 42, null");
	}

}

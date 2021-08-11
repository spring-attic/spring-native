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

import java.util.List;

import com.squareup.javapoet.CodeBlock;
import org.junit.jupiter.api.Test;

import org.springframework.context.bootstrap.generator.test.CodeSnippet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MultiStatement}.
 *
 * @author Stephane Nicoll
 */
class MultiStatementTests {

	@Test
	void isEmptyWithNoStatement() {
		assertThat(new MultiStatement().isEmpty()).isTrue();
	}

	@Test
	void isEmptyWithStatement() {
		MultiStatement statements = new MultiStatement();
		statements.add(CodeBlock.of("int i = 0"));
		assertThat(statements.isEmpty()).isFalse();
	}

	@Test
	void singleStatement() {
		MultiStatement statements = new MultiStatement();
		statements.add("field.method($S)", "hello");
		CodeBlock codeBlock = statements.toCodeBlock();
		assertThat(CodeSnippet.of(codeBlock)).isEqualTo("field.method(\"hello\")");
	}

	@Test
	void multiStatements() {
		MultiStatement statements = new MultiStatement();
		statements.add("field.method($S)", "hello");
		statements.add("field.anotherMethod($S)", "hello");
		CodeBlock codeBlock = statements.toCodeBlock();
		assertThat(CodeSnippet.of(codeBlock)).lines().containsExactly(
				"field.method(\"hello\");",
				"field.anotherMethod(\"hello\");");
	}

	@Test
	void singleStatementWithLambda() {
		MultiStatement statements = new MultiStatement();
		statements.add("field.method($S)", "hello");
		CodeBlock codeBlock = statements.toCodeBlock(CodeBlock.of("() ->"));
		assertThat(CodeSnippet.of(codeBlock)).isEqualTo("() -> field.method(\"hello\")");
	}

	@Test
	void multiStatementsWithLambda() {
		MultiStatement statements = new MultiStatement();
		statements.add("field.method($S)", "hello");
		statements.add("field.anotherMethod($S)", "hello");
		CodeBlock codeBlock = statements.toCodeBlock(CodeBlock.of("() ->"));
		assertThat(CodeSnippet.of(codeBlock)).lines().containsExactly(
				"() -> {",
				"  field.method(\"hello\");",
				"  field.anotherMethod(\"hello\");",
				"}");
	}

	@Test
	void multiStatementsWithAddAll() {
		MultiStatement statements = new MultiStatement();
		statements.addAll(List.of(0, 1, 2), (index) -> CodeBlock.of("field[$L] = $S", index, "hello"));
		CodeBlock codeBlock = statements.toCodeBlock("() ->");
		assertThat(CodeSnippet.of(codeBlock)).lines().containsExactly(
				"() -> {",
				"  field[0] = \"hello\";",
				"  field[1] = \"hello\";",
				"  field[2] = \"hello\";",
				"}");
	}

}

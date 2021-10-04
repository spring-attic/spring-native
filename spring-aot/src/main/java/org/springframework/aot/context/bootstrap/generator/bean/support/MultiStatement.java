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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;

/**
 * A {@link CodeBlock} wrapper for multiple statements.
 *
 * @author Stephane Nicoll
 */
public final class MultiStatement {

	private final List<CodeBlock> statements = new ArrayList<>();

	/**
	 * Specify if this instance is empty.
	 * @return {@code true} if no statement is registered, {@code false} otherwise
	 */
	public boolean isEmpty() {
		return this.statements.isEmpty();
	}

	/**
	 * Add a statement.
	 * @param statement the statement to add
	 */
	public void add(CodeBlock statement) {
		this.statements.add(statement);
	}

	/**
	 * Add a statement.
	 * @param code the code of the statement
	 * @param args the arguments for placeholders
	 * @see CodeBlock#of(String, Object...)
	 */
	public void add(String code, Object... args) {
		add(CodeBlock.of(code, args));
	}

	/**
	 * Add the statements produced from the {@code itemGenerator} applied on the specified
	 * items.
	 * @param items the items to handle, each item is represented as a statement
	 * @param itemGenerator the item generator
	 * @param <T> the type of the item
	 */
	public <T> void addAll(Iterable<T> items, Function<T, CodeBlock> itemGenerator) {
		items.forEach((element) -> add(itemGenerator.apply(element)));
	}

	/**
	 * Return a {@link CodeBlock} that applies all the {@code statements} of this
	 * instance. If only one statement is available, it is not completed using the
	 * {@code ;} termination so that it can be used in the context of a lambda.
	 * @return the statement(s)
	 */
	public CodeBlock toCodeBlock() {
		Builder code = CodeBlock.builder();
		for (int i = 0; i < this.statements.size(); i++) {
			code.add(statements.get(i));
			if (i < this.statements.size() - 1) {
				code.add(";\n");
			}
		}
		if (this.isMulti()) {
			code.add(";");
		}
		return code.build();
	}

	/**
	 * Return a {@link CodeBlock} that applies all the {@code statements} of this
	 * instance in the context of a lambda.
	 * @param lambda the context of the lambda, must end with {@code ->}
	 * @return the lambda body
	 */
	public CodeBlock toCodeBlock(CodeBlock lambda) {
		Builder code = CodeBlock.builder();
		code.add(lambda);
		if (isMulti()) {
			code.beginControlFlow("");
		}
		else {
			code.add(" ");
		}
		code.add(toCodeBlock());
		if (isMulti()) {
			code.add("\n").unindent().add("}");
		}
		return code.build();
	}

	/**
	 * Return a {@link CodeBlock} that applies all the {@code statements} of this
	 * instance in the context of a lambda.
	 * @param lambda the context of the lambda, must end with {@code ->}
	 * @return the lambda body
	 */
	public CodeBlock toCodeBlock(String lambda) {
		return toCodeBlock(CodeBlock.of(lambda));
	}

	private boolean isMulti() {
		return this.statements.size() > 1;
	}
}

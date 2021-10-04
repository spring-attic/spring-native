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

package org.springframework.aot.context.bootstrap.generator.test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.ListAssert;

/**
 * Assertions for text content.
 *
 * @author Stephane Nicoll
 */
public class TextAssert extends AbstractStringAssert<TextAssert> {

	private final String original;

	private final int indent;

	private TextAssert(String original, int indent, String actual) {
		super(actual, TextAssert.class);
		this.original = original;
		this.indent = indent;
		describedAs("Actual content has %s indent level(s) removed", this.indent);
	}

	public TextAssert(String actual) {
		this(actual, 0, actual);
	}

	/**
	 * Return an {@link ListAssert assert} for the lines that constitute this text, to
	 * allow chaining of lines-specific assertions from this call.
	 * @return a {@link ListAssert} for the lines that constitutes this text
	 */
	public ListAssert<String> lines() {
		return new IndentAwareStringListAssert(readAllLines(this.original), readAllLines(this.actual), this.indent);
	}

	/**
	 * Return a {@link TextAssert} where the content to assert has the number of specified
	 * indent levels removed.
	 * @param indent the number of indent level to remove
	 * @return a new instance where assertions can rely on the specified of indent levels removed
	 */
	public TextAssert removeIndent(int indent) {
		int charsToRemove = indent * 2;
		List<String> strings = readAllLines(this.actual);
		return new TextAssert(this.actual, indent, strings.stream()
				.map((line) -> (line.length() > charsToRemove) ? line.substring(charsToRemove) : line)
				.collect(Collectors.joining("\n")));
	}

	private static List<String> readAllLines(String source) {
		String[] lines = source.split("\\r?\\n");
		return Arrays.asList(lines);
	}

	private static class IndentAwareStringListAssert extends ListAssert<String> {

		private final List<String> original;

		private final int indent;

		public IndentAwareStringListAssert(List<String> original, List<String> actual, int indent) {
			super(actual);
			this.original = original;
			this.indent = indent;
			describedAs("Actual content has %s indent level(s) removed", this.indent);
		}

	}

}

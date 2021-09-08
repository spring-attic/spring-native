package org.springframework.context.bootstrap.generator.test;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.ListAssert;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertions for a {@link CodeSnippet code snippet}.
 *
 * @author Stephane Nicoll
 */
public class CodeSnippetAssert extends AbstractStringAssert<CodeSnippetAssert> {

	private final List<String> file;

	public CodeSnippetAssert(CodeSnippet codeSnippet) {
		super(codeSnippet.getSnippet(), CodeSnippetAssert.class);
		this.file = readAllLines(codeSnippet.getFileContent());
	}

	/**
	 * Return an {@link ListAssert assert} for the lines that constitute this text, to
	 * allow chaining of lines-specific assertions from this call.
	 * @return a {@link ListAssert} for the lines that constitutes this text
	 */
	public ListAssert<String> lines() {
		return new ListAssert<>(readAllLines(this.actual));
	}

	/**
	 * Assert that the generated content produced an import statement for the specified
	 * class.
	 * @param type the class that should be imported
	 */
	public CodeSnippetAssert hasImport(Class<?> type) {
		assertThat(this.file).contains(String.format("import %s;", type.getName()));
		return this;
	}

	private static List<String> readAllLines(String source) {
		String[] lines = source.split("\\r?\\n");
		return Arrays.asList(lines);
	}

}

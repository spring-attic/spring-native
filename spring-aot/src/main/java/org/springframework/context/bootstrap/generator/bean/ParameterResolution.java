package org.springframework.context.bootstrap.generator.bean;

import java.lang.reflect.Parameter;

import com.squareup.javapoet.CodeBlock;

/**
 * An executable's {@link Parameter} resolution.
 *
 * @author Stephane Nicoll
 */
class ParameterResolution {

	private final CodeBlock assignment;

	private final CodeBlock parameter;

	private ParameterResolution(CodeBlock parameter, CodeBlock assignment) {
		this.assignment = assignment;
		this.parameter = parameter;
	}

	/**
	 * Create a resolution for a parameter that does not require an assignment.
	 * @param code the necessary code to resolve the parameter
	 * @return a new instance
	 */
	static ParameterResolution ofParameter(CodeBlock code) {
		return new ParameterResolution(code, null);
	}

	/**
	 * Create a resolution for a parameter that does require an assignment.
	 * @param assignment the necessary code to assign a variable that refers to the parameter
	 * @param parameter the necessary code to use the assignment to resolve the parameter
	 * @return a new instance
	 */
	static ParameterResolution ofAssignableParameter(CodeBlock assignment, CodeBlock parameter) {
		return new ParameterResolution(parameter, assignment);
	}

	/**
	 * Whether this parameter requires an assignment.
	 * @return {@code true} if it requires a intermediate variable
	 */
	boolean hasAssignment() {
		return this.assignment != null;
	}

	void applyAssignment(CodeBlock.Builder builder) {
		if (this.assignment != null) {
			builder.add("$L;\n", this.assignment);
		}
	}

	void applyParameter(CodeBlock.Builder builder) {
		if (this.parameter != null) {
			builder.add(this.parameter);
		}
	}

}

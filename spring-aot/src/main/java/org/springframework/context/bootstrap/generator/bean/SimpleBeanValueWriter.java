package org.springframework.context.bootstrap.generator.bean;

import java.util.function.Consumer;

import com.squareup.javapoet.CodeBlock.Builder;

import org.springframework.context.bootstrap.generator.bean.descriptor.BeanInstanceDescriptor;

/**
 * Simple {@link BeanValueWriter} that delegates code generation to a {@link Consumer}.
 *
 * @author Stephane Nicoll
 */
public final class SimpleBeanValueWriter implements BeanValueWriter {

	private final BeanInstanceDescriptor descriptor;

	private final Consumer<Builder> code;

	public SimpleBeanValueWriter(BeanInstanceDescriptor descriptor, Consumer<Builder> code) {
		this.descriptor = descriptor;
		this.code = code;
	}

	@Override
	public BeanInstanceDescriptor getDescriptor() {
		return this.descriptor;
	}

	@Override
	public void writeValueSupplier(Builder code) {
		this.code.accept(code);
	}

}

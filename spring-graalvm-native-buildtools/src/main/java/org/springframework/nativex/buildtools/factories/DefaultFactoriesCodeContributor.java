package org.springframework.nativex.buildtools.factories;

import java.util.function.Consumer;

import com.squareup.javapoet.CodeBlock;

import org.springframework.nativex.type.Method;

/**
 * {@link FactoriesCodeContributor} that handles default, public constructors.
 *
 * @author Brian Clozel
 */
class DefaultFactoriesCodeContributor implements FactoriesCodeContributor {

	@Override
	public boolean canContribute(SpringFactory springFactory) {
		Method defaultConstructor = springFactory.getFactory().getDefaultConstructor();
		return (defaultConstructor != null && defaultConstructor.isPublic());
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code) {
		code.writeToStaticBlock(generateStaticInit(factory));
	}

	Consumer<CodeBlock.Builder> generateStaticInit(SpringFactory factory) {
		return builder ->
				builder.addStatement("factories.add($N.class, new $N())", factory.getFactoryType().getDottedName(),
						factory.getFactory().getDottedName().replace('$', '.'));
	}
}

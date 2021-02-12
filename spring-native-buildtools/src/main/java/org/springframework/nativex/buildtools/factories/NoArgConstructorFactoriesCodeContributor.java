package org.springframework.nativex.buildtools.factories;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.nativex.buildtools.BuildContext;
import org.springframework.nativex.domain.reflect.ClassDescriptor;

import com.squareup.javapoet.ClassName;

/**
 * {@link FactoriesCodeContributor} that contributes source code for some factories
 * that are missing a no-arg constructor and require injection of specific parameters.
 * <p>Instead of instantiating them statically, we're making sure that
 * {@link org.springframework.core.io.support.SpringFactoriesLoader#loadFactoryNames(Class, ClassLoader)}
 * will return their name and we're also adding reflection metadata for native images.
 *
 * @author Brian Clozel
 */
public class NoArgConstructorFactoriesCodeContributor implements FactoriesCodeContributor {

	private final Log logger = LogFactory.getLog(NoArgConstructorFactoriesCodeContributor.class);

	@Override
	public boolean canContribute(SpringFactory factory) {
		return !factory.getFactory().getDefaultConstructor().isPresent();
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		ClassName factoryTypeClass = ClassName.bestGuess(factory.getFactoryType().getClassName());
		generateReflectionMetadata(factory.getFactory().getClassName(), context);
		code.writeToStaticBlock(builder -> {
			builder.addStatement("names.add($T.class, $S)", factoryTypeClass,
					factory.getFactory().getClassName());
		});
	}

	private void generateReflectionMetadata(String factoryClassName, BuildContext context) {
		ClassDescriptor factoryDescriptor = ClassDescriptor.of(factoryClassName);
		//factoryDescriptor.setFlag(Flag.allDeclaredConstructors);
		context.describeReflection(reflect -> reflect.add(factoryDescriptor));
	}

}

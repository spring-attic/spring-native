package org.springframework.nativex.buildtools.factories;

import com.squareup.javapoet.ClassName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.nativex.buildtools.BuildContext;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.Flag;
import org.springframework.nativex.type.Method;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;

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
		Method defaultConstructor = factory.getFactory().getDefaultConstructor();
		return defaultConstructor == null ||
				defaultConstructor.hasAnnotation("Ljava/lang/Deprecated;", false);
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		ClassName factoryTypeClass = ClassName.bestGuess(factory.getFactoryType().getDottedName());
		generateReflectionMetadata(factory.getFactory(), context);
		code.writeToStaticBlock(builder -> {
			builder.addStatement("names.add($T.class, $S)", factoryTypeClass,
					factory.getFactory().getDottedName());
		});
	}

	private void generateReflectionMetadata(Type factory, BuildContext context) {
		ClassDescriptor factoryDescriptor = ClassDescriptor.of(factory.getDottedName());
		//factoryDescriptor.setFlag(Flag.allDeclaredConstructors);
		context.describeReflection(reflect -> reflect.add(factoryDescriptor));
	}

	@Override
	public boolean passesAnyConditionalOnClass(TypeSystem typeSystem, SpringFactory factory) {
		// TODO Auto-generated method stub
		return false;
	}

}

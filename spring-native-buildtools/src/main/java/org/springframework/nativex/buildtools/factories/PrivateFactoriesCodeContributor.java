package org.springframework.nativex.buildtools.factories;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.nativex.buildtools.BuildContext;
import org.springframework.nativex.type.Type;
import org.springframework.nativex.type.TypeSystem;
import org.springframework.util.StringUtils;

/**
 * {@link FactoriesCodeContributor} that handles default constructors which are not public.
 *
 * @author Brian Clozel
 */
class PrivateFactoriesCodeContributor implements FactoriesCodeContributor {

	private final Log logger = LogFactory.getLog(PrivateFactoriesCodeContributor.class);

	@Override
	public boolean canContribute(SpringFactory springFactory) {
		Type factory = springFactory.getFactory();
		return !factory.isPublic() || factory.getDefaultConstructor() == null
				|| !factory.getDefaultConstructor().isPublic();
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		String packageName = factory.getFactory().getPackageName();
		ClassName factoryTypeClass = ClassName.bestGuess(factory.getFactoryType().getDottedName());
		ClassName factoryClass = ClassName.bestGuess(factory.getFactory().getDottedName());
		ClassName staticFactoryClass = ClassName.get(packageName, code.getStaticFactoryClass(packageName).name);
		MethodSpec creator = MethodSpec.methodBuilder(StringUtils.uncapitalize(factory.getFactory().getSimpleName()))
				.addModifiers(javax.lang.model.element.Modifier.PUBLIC, javax.lang.model.element.Modifier.STATIC)
				.returns(factoryClass)
				.addStatement("return new $T()", factoryClass).build();
		code.writeToStaticFactoryClass(packageName, builder -> builder.addMethod(creator));
		code.writeToStaticBlock(block -> {
			block.addStatement("factories.add($T.class, () -> $T.$N())", factoryTypeClass, staticFactoryClass, creator);
		});
	}

	@Override
	public boolean passesAnyConditionalOnClass(TypeSystem typeSystem, SpringFactory factory) {
		// TODO Auto-generated method stub
		return false;
	}

}

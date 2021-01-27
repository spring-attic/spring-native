package org.springframework.nativex.buildtools.factories;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.type.classreading.ClassDescriptor;
import org.springframework.core.type.classreading.MethodDescriptor;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.buildtools.BuildContext;
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
		ClassDescriptor factory = springFactory.getFactory();
		return !factory.isPublic() || !factory.getDefaultConstructor().map(MethodDescriptor::isPublic).orElse(false);
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		TypeSystem typeSystem = context.getTypeSystem();
		boolean factoryOK = 
				passesAnyConditionalOnClass(typeSystem, factory);
		if (factoryOK) {
			String packageName = factory.getFactory().getPackageName();
			ClassName factoryTypeClass = ClassName.bestGuess(factory.getFactoryType().getCanonicalClassName());
			ClassName factoryClass = ClassName.bestGuess(factory.getFactory().getCanonicalClassName());
			ClassName staticFactoryClass = ClassName.get(packageName, code.getStaticFactoryClass(packageName).name);
			MethodSpec creator = MethodSpec.methodBuilder(StringUtils.uncapitalize(factory.getFactory().getShortName()))
					.addModifiers(javax.lang.model.element.Modifier.PUBLIC, javax.lang.model.element.Modifier.STATIC)
					.returns(factoryClass)
					.addStatement("return new $T()", factoryClass).build();
			code.writeToStaticFactoryClass(packageName, builder -> builder.addMethod(creator));
			code.writeToStaticBlock(block -> {
				block.addStatement("factories.add($T.class, () -> $T.$N())", factoryTypeClass, staticFactoryClass, creator);
			});
		}
	}

	@Override
	public boolean passesAnyConditionalOnWebApplication(TypeSystem typeSystem, SpringFactory factory) {
		// TODO Auto-generated method stub
		return false;
	}

}

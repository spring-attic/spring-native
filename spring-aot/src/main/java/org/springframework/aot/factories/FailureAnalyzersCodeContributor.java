package org.springframework.aot.factories;

import com.squareup.javapoet.ClassName;

import org.springframework.aot.build.context.BuildContext;
import org.springframework.boot.diagnostics.FailureAnalyzer;
import org.springframework.nativex.hint.TypeAccess;

/**
 * Contributor for {@code FailureAnalyzer} that need to be handled via reflection as of Spring Boot 2.7.
 */
public class FailureAnalyzersCodeContributor implements FactoriesCodeContributor {

	@Override
	public boolean canContribute(SpringFactory springFactory) {
		return springFactory.getFactoryType() == FailureAnalyzer.class;
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		ClassName factoryTypeClass = ClassName.bestGuess(factory.getFactoryType().getCanonicalName());
		code.writeToStaticBlock(builder -> {
			builder.addStatement("names.add($T.class, $S)", factoryTypeClass,
					factory.getFactory().getCanonicalName());
		});
		generateReflectionMetadata(factory.getFactory().getName(), context);
	}

	private void generateReflectionMetadata(String factoryClassName, BuildContext context) {
		org.springframework.nativex.domain.reflect.ClassDescriptor factoryDescriptor = org.springframework.nativex.domain.reflect.ClassDescriptor.of(factoryClassName);
		factoryDescriptor.setAccess(TypeAccess.DECLARED_CONSTRUCTORS);
		context.describeReflection(reflect -> reflect.add(factoryDescriptor));
	}
}

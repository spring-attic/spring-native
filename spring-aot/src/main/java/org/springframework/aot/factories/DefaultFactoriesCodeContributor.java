package org.springframework.aot.factories;

import java.util.function.Consumer;

import com.squareup.javapoet.CodeBlock;

import org.springframework.aot.BuildContext;
import org.springframework.core.type.classreading.MethodDescriptor;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.support.ConfigOptions;

/**
 * {@link FactoriesCodeContributor} that handles default, public constructors.
 *
 * @author Brian Clozel
 * @author Sebasten Deleuze
 */
class DefaultFactoriesCodeContributor implements FactoriesCodeContributor {

	@Override
	public boolean canContribute(SpringFactory springFactory) {
		return springFactory.getFactory().getDefaultConstructor().filter(MethodDescriptor::isPublic).isPresent();
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		TypeSystem typeSystem = context.getTypeSystem();
		boolean factoryOK =
				passesAnyConditionalOnClass(typeSystem, factory) &&
						passesFilterCheck(typeSystem, factory) &&
						passesAnyConditionalOnWebApplication(typeSystem, factory);
		if (factoryOK) {
			code.writeToStaticBlock(generateStaticInit(factory));
			// TODO To be removed, currently required due to org.springframework.boot.env.ReflectionEnvironmentPostProcessorsFactory
			if (factory.getFactoryType().getClassName().endsWith("EnvironmentPostProcessor")) {
				generateReflectionMetadata(factory.getFactory().getClassName(), context);
			}
		}
	}

	Consumer<CodeBlock.Builder> generateStaticInit(SpringFactory factory) {
		return builder ->
				builder.addStatement("factories.add($N.class, () -> new $N())", factory.getFactoryType().getCanonicalClassName(),
						factory.getFactory().getCanonicalClassName());
	}

	// See the SpringBootFactories code in the substitutions module for more of these we need to factor in/support some how.
	private boolean passesFilterCheck(TypeSystem typeSystem, SpringFactory factory) {
		String factoryName = factory.getFactory().getClassName();
		// TODO shame no ConditionalOnClass on these providers
		if (factoryName.endsWith("FreeMarkerTemplateAvailabilityProvider")) {
			return typeSystem.resolveClass("freemarker.template.Configuration") != null;
		}
		if (factoryName.endsWith("MustacheTemplateAvailabilityProvider")) {
			return typeSystem.resolveClass("com.samskivert.mustache.Mustache") != null;
		}
		if (factoryName.endsWith("GroovyTemplateAvailabilityProvider")) {
			return typeSystem.resolveClass("groovy.text.TemplateEngine") != null;
		}
		if (factoryName.endsWith("ThymeleafTemplateAvailabilityProvider")) {
			return typeSystem.resolveClass("org.thymeleaf.spring5.SpringTemplateEngine") != null;
		}
		if (factoryName.endsWith("JspTemplateAvailabilityProvider")) {
			return typeSystem.resolveClass("org.apache.jasper.compiler.JspConfig") != null;
		}
		if (factoryName.equals("org.springframework.boot.autoconfigure.BackgroundPreinitializer")) {
			return false;
		}
		if (factoryName.equals("org.springframework.boot.env.YamlPropertySourceLoader")) {
			// to trigger this... mvn -Dspring.native.remove-yaml-support=true
			return !ConfigOptions.shouldRemoveYamlSupport();
		}
		return true;
	}

	private void generateReflectionMetadata(String factoryClassName, BuildContext context) {
		ClassDescriptor factoryDescriptor = ClassDescriptor.of(factoryClassName);
		factoryDescriptor.setFlag(Flag.allPublicConstructors);
		context.describeReflection(reflect -> reflect.add(factoryDescriptor));
	}

}

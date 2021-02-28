package org.springframework.aot.factories;

import java.util.function.Consumer;

import com.squareup.javapoet.CodeBlock;

import org.springframework.aot.BuildContext;
import org.springframework.core.type.classreading.MethodDescriptor;
import org.springframework.core.type.classreading.TypeSystem;
import org.springframework.nativex.AotOptions;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.hint.Flag;

/**
 * {@link FactoriesCodeContributor} that handles default, public constructors.
 *
 * @author Brian Clozel
 * @author Sebasten Deleuze
 */
class DefaultFactoriesCodeContributor implements FactoriesCodeContributor {

	private final AotOptions aotOptions;

	DefaultFactoriesCodeContributor(AotOptions aotOptions) {
		this.aotOptions = aotOptions;
	}

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

	private boolean passesFilterCheck(TypeSystem typeSystem, SpringFactory factory) {
		String factoryName = factory.getFactory().getClassName();
		// TODO shame no ConditionalOnClass on these providers
		if (factoryName.endsWith("FreeMarkerTemplateAvailabilityProvider")) {
			return typeSystem.resolveClass("freemarker.template.Configuration") != null;
		} else if (factoryName.endsWith("MustacheTemplateAvailabilityProvider")) {
			return typeSystem.resolveClass("com.samskivert.mustache.Mustache") != null;
		} else if (factoryName.endsWith("GroovyTemplateAvailabilityProvider")) {
			return typeSystem.resolveClass("groovy.text.TemplateEngine") != null;
		} else if (factoryName.endsWith("ThymeleafTemplateAvailabilityProvider")) {
			return typeSystem.resolveClass("org.thymeleaf.spring5.SpringTemplateEngine") != null;
		} else if (factoryName.endsWith("JspTemplateAvailabilityProvider")) {
			return typeSystem.resolveClass("org.apache.jasper.compiler.JspConfig") != null;
		} else if (factoryName.equals("org.springframework.boot.autoconfigure.BackgroundPreinitializer")) {
			return false;
		} else if (factoryName.equals("org.springframework.boot.env.YamlPropertySourceLoader")) {
			return !aotOptions.isRemoveYamlSupport();
		} else if (factoryName.startsWith("org.springframework.boot.devtools")) {
			throw new IllegalStateException("Devtools is not supported yet, please remove the related dependency for now.");
		}
		return true;
	}

	private void generateReflectionMetadata(String factoryClassName, BuildContext context) {
		ClassDescriptor factoryDescriptor = ClassDescriptor.of(factoryClassName);
		factoryDescriptor.setFlag(Flag.allPublicConstructors);
		context.describeReflection(reflect -> reflect.add(factoryDescriptor));
	}

}

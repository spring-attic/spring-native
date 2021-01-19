package org.springframework.nativex.buildtools.factories;

import java.util.function.Consumer;

import com.squareup.javapoet.CodeBlock;

import org.springframework.nativex.buildtools.BuildContext;
import org.springframework.nativex.type.Method;
import org.springframework.nativex.type.TypeSystem;

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
	public void contribute(SpringFactory factory, CodeGenerator code, BuildContext context) {
		TypeSystem typeSystem = factory.getFactory().getTypeSystem();
		boolean factoryOK = 
			passesAnyConditionalOnClass(typeSystem, factory) &&
			passesFilterCheck(typeSystem, factory);
		if (factoryOK) {
			code.writeToStaticBlock(generateStaticInit(factory));
		}
	}

	Consumer<CodeBlock.Builder> generateStaticInit(SpringFactory factory) {
		return builder ->
				builder.addStatement("factories.add($N.class, $N::new)", factory.getFactoryType().getDottedName(),
						factory.getFactory().getDottedName().replace('$', '.'));
	}

	// See the SpringBootFactories code in the substitutions module for more of these we need to factor in/support some how.
	private boolean passesFilterCheck(TypeSystem typeSystem, SpringFactory factory) {
		String factoryName = factory.getFactory().getDottedName();
		// TODO shame no ConditionalOnClass on these providers
		if (factoryName.endsWith("FreeMarkerTemplateAvailabilityProvider")) {
			return typeSystem.resolveDotted("freemarker.template.Configuration",true)!=null;
		}
		if (factoryName.endsWith("MustacheTemplateAvailabilityProvider")) {
			return typeSystem.resolveDotted("com.samskivert.mustache.Mustache",true)!=null;
		}
		if (factoryName.endsWith("GroovyTemplateAvailabilityProvider")) {
			return typeSystem.resolveDotted("groovy.text.TemplateEngine",true)!=null;
		}
		if (factoryName.endsWith("ThymeleafTemplateAvailabilityProvider")) {
			return typeSystem.resolveDotted("org.thymeleaf.spring5.SpringTemplateEngine",true)!=null;
		}
		if (factoryName.endsWith("JspTemplateAvailabilityProvider")) {
			return typeSystem.resolveDotted("org.apache.jasper.compiler.JspConfig",true)!=null;
		}
		if (factoryName.equals("org.springframework.boot.autoconfigure.BackgroundPreinitializer")) {
			return false;
		}
		return true;
	}
}

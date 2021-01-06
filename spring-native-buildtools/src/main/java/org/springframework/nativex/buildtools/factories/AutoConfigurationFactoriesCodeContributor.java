package org.springframework.nativex.buildtools.factories;

import java.util.List;
import java.util.Optional;

import com.squareup.javapoet.ClassName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.nativex.type.TypeSystem;

/**
 * {@link FactoriesCodeContributor} that contributes source code for {@code EnableAutoConfiguration} factories.
 * <p>Instead of instantiating them statically, we're making sure that
 * {@link org.springframework.core.io.support.SpringFactoriesLoader#loadFactoryNames(Class, ClassLoader)}
 * will return their name and we're also adding reflection metadata for native images.
 * <p>For optimization purposes, this contributor can also ignore auto-configurations with
 * conditional annotations that will not match at runtime.
 *
 * @author Brian Clozel
 */
public class AutoConfigurationFactoriesCodeContributor implements FactoriesCodeContributor {

	private static String AUTO_CONFIGURATION_TYPE = "org.springframework.boot.autoconfigure.EnableAutoConfiguration";

	private final Log logger = LogFactory.getLog(AutoConfigurationFactoriesCodeContributor.class);


	@Override
	public boolean canContribute(SpringFactory factory) {
		return AUTO_CONFIGURATION_TYPE.equals(factory.getFactoryType().getDottedName());
	}

	@Override
	public void contribute(SpringFactory factory, CodeGenerator code) {
		// TODO: contribute reflection data
		List<String> conditionClasses = factory.getFactory().findConditionalOnClassValue();
		TypeSystem typeSystem = factory.getFactory().getTypeSystem();
		Optional<String> missingConditionClass = conditionClasses.stream()
				.filter(conditionClass -> typeSystem.Lresolve(conditionClass, true) == null)
				.findAny();
		if (!missingConditionClass.isPresent()) {
			ClassName factoryTypeClass = ClassName.bestGuess(factory.getFactoryType().getDottedName());
			code.writeToStaticBlock(builder -> {
				builder.addStatement("names.add($T.class, $S)", factoryTypeClass,
						factory.getFactory().getDottedName());
			});
		}
	}



}

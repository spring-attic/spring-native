package org.springframework.context.annotation;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.context.annotation.Import;

/*
// Not quite right... this is a superclass of a selector we've already added...
proposedHints.put(AdviceModeImportSelector,
		new CompilationHint(true, true, new String[0]
		));
		*/
@ConfigurationHint(value = AdviceModeImportSelector.class, abortIfTypesMissing = true, follow=true)
// TODO can be {@link Configuration}, {@link ImportSelector}, {@link ImportBeanDefinitionRegistrar}
// @Imports has @CompilationHint(skipIfTypesMissing=false?, follow=true)
@ConfigurationHint(value = Import.class, abortIfTypesMissing = false, follow=true) // TODO verify these flags...
@ConfigurationHint(value = Conditional.class, extractTypesFromAttributes = { "value" }) // TODO need extract?
public class Hints implements NativeImageConfiguration {
}

package org.springframework.boot.context.properties;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;

//THis one seems gone in latest spring??!
//EnableConfigurationPropertiesImportSelector has
//@CompilationHint(skipIfTypesMissing=true, follow=false, name={
//	 	"org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector$ConfigurationPropertiesBeanRegistrar",
//	 	"org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessorRegistrar"})
//proposedHints.put(EnableConfigurationPropertiesImportSelector,
//		new CompilationHint(false,false, new String[] {
//		 	"org.springframework.boot.context.properties.EnableConfigurationPropertiesImportSelector$ConfigurationPropertiesBeanRegistrar:REGISTRAR",
//		 	"org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessorRegistrar:REGISTRAR"}
//		));	
//@ConfigurationHint(value=EnableConfigurationPropertiesImportSelector.class)
//TODO do configuration properties chain?
//@EnableConfigurationProperties has @CompilationHint(skipIfTypesMissing=false, follow=false)
@ConfigurationHint(value = EnableConfigurationProperties.class)
public class Hints implements NativeImageConfiguration {
}

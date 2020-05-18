/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.context.properties;

import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;

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
@NativeImageHint(trigger = EnableConfigurationPropertiesRegistrar.class,typeInfos = {
	@TypeInfo(
			// TODO version handling - this type isn't in 2.2.3 but in 2.3.0.M2 ! How can we make sure
			// we keep up and verify when things might not be compatible?
			typeNames="org.springframework.boot.context.properties.BoundConfigurationProperties",
			types= {
			ConfigurationPropertiesBindingPostProcessor.class,
			ConfigurationPropertiesBinder.Factory.class, 
			ConfigurationPropertiesBeanDefinitionValidator.class,
			ConfigurationPropertiesBinder.class,
			DeprecatedConfigurationProperty.class,
			NestedConfigurationProperty.class
			})	
})
@NativeImageHint(trigger = EnableConfigurationProperties.class)
@NativeImageHint(typeInfos = {@TypeInfo(types= {
		ConfigurationPropertiesScan.class,ConfigurationPropertiesScanRegistrar.class,
		ConfigurationBeanFactoryMetadata.class
		})})
public class Hints implements NativeImageConfiguration {
}

package org.springframework.data.web.config;

import org.springframework.data.web.config.EnableSpringDataWebSupport.QuerydslActivator;
import org.springframework.data.web.config.EnableSpringDataWebSupport.SpringDataWebConfigurationImportSelector;
import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;


/*
//  EnableSpringDataWebSupport. TODO: there are others in spring.factories.
proposedHints.put(SpringDataWebConfigurationSelector,
		new CompilationHint(true, true, new String[] {
			"org.springframework.data.web.config.HateoasAwareSpringDataWebConfiguration",
			"org.springframework.data.web.config.SpringDataWebConfiguration"
		}));
*/
@ConfigurationHint(value = SpringDataWebConfigurationImportSelector.class, typeInfos = {
	@TypeInfo(types= {HateoasAwareSpringDataWebConfiguration.class,SpringDataWebConfiguration.class})	
},abortIfTypesMissing = true,follow=true) 
/*
public final static String SpringDataWebQueryDslSelector = "Lorg/springframework/data/web/config/EnableSpringDataWebSupport$QuerydslActivator;";
//  EnableSpringDataWebSupport. TODO: there are others in spring.factories.
proposedHints.put(SpringDataWebQueryDslSelector,
		new CompilationHint(true, true, new String[] {
			"org.springframework.data.web.config.QuerydslWebConfiguration"}
		));
*/
@ConfigurationHint(value = QuerydslActivator.class, typeInfos = {
	@TypeInfo(types= {QuerydslWebConfiguration.class})	
},abortIfTypesMissing = true,follow=true) 
public class Hints implements NativeImageConfiguration {
}

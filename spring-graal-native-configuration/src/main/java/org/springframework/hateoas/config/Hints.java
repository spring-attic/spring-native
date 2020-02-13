package org.springframework.hateoas.config;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;

/*
proposedHints.put(WebStackImportSelector,
		new CompilationHint(false, true, new String[] {
			//"org.springframework.hateoas.config.WebStackImportSelector" - why was this here???
			"org.springframework.hateoas.config.WebMvcHateoasConfiguration",
			"org.springframework.hateoas.config.WebFluxHateoasConfiguration"
		}));
		*/
@ConfigurationHint(value=WebStackImportSelector.class,typeInfos= {
	@TypeInfo(types= {WebMvcHateoasConfiguration.class,WebFluxHateoasConfiguration.class})	
},follow=true)
/*
public final static String HypermediaConfigurationImportSelector = "Lorg/springframework/hateoas/config/HypermediaConfigurationImportSelector;";
	// TODO I am not sure the specific entry here is right, but given that the selector references entries loaded via factories - aren't those already handled? 
	proposedHints.put(HypermediaConfigurationImportSelector,
			new CompilationHint(false, true, new String[] {
					"org.springframework.hateoas.config.HypermediaConfigurationImportSelector"
			}));
			*/
@ConfigurationHint(value=HypermediaConfigurationImportSelector.class,typeInfos= {
	@TypeInfo(types= {HypermediaConfigurationImportSelector.class})	
},follow=true) // TODO WTF is this one?
public class Hints implements NativeImageConfiguration {
}

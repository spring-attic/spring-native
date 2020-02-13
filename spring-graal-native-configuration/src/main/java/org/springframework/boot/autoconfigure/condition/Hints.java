package org.springframework.boot.autoconfigure.condition;

import org.springframework.graal.extension.ConfigurationHint;
import org.springframework.graal.extension.NativeImageConfiguration;
import org.springframework.graal.extension.TypeInfo;
import org.springframework.web.context.support.GenericWebApplicationContext;

/*
proposedHints.put("Lorg/springframework/boot/autoconfigure/condition/OnWebApplicationCondition;", 
		new CompilationHint(false, false, new String[] {
			"org.springframework.web.context.support.GenericWebApplicationContext",
			
		}));
*/
@ConfigurationHint(value=OnWebApplicationCondition.class,typeInfos = {
	@TypeInfo(types= {GenericWebApplicationContext.class})	
})
@ConfigurationHint(value=ConditionalOnWebApplication.class, 
	typeInfos= {@TypeInfo(types= {GenericWebApplicationContext.class, ConditionalOnWebApplication.Type.class})}, abortIfTypesMissing = true)
@ConfigurationHint(value = ConditionalOnSingleCandidate.class, extractTypesFromAttributes = { "value",
		"type" }, abortIfTypesMissing = true)
@ConfigurationHint(value = ConditionalOnClass.class, extractTypesFromAttributes = { "value",
		"name" }, abortIfTypesMissing = true)
// Here exposing SearchStrategy as it is the type of a field within the annotation. 
// TODO Feels like all conditions should just get this 'for free'
@ConfigurationHint(value = ConditionalOnMissingBean.class, extractTypesFromAttributes = { "value", "name" }, typeInfos = {
		@TypeInfo(types= {SearchStrategy.class}) }, abortIfTypesMissing = true)
public class Hints implements NativeImageConfiguration {
}

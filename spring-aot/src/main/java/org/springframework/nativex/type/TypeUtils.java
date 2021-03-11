/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.nativex.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.asm.tree.AnnotationNode;

import org.springframework.nativex.AotOptions;

/**
 * 
 * @author Andy Clement
 */
public class TypeUtils {

	private static Log logger = LogFactory.getLog(TypeUtils.class);	

	public final static String AtConditionalOnProperty = "Lorg/springframework/boot/autoconfigure/condition/ConditionalOnProperty;";
	public final static String AtConditionalOnAvailableEndpoint = "Lorg/springframework/boot/actuate/autoconfigure/endpoint/condition/ConditionalOnAvailableEndpoint;";
	public final static String AtConditionalOnEnabledHealthIndicator = "Lorg/springframework/boot/actuate/autoconfigure/health/ConditionalOnEnabledHealthIndicator;";
	public final static String AtConditionalOnEnabledMetricsExport = "Lorg/springframework/boot/actuate/autoconfigure/metrics/export/ConditionalOnEnabledMetricsExport;";
	public final static String AtEndpoint = "Lorg/springframework/boot/actuate/endpoint/annotation/Endpoint;";
	
	/**
	 * Find any @ConditionalOnProperty and see if the specified property should be checked at build time. If it should be and
	 * fails the check, return the property name in question.
	 * 
	 * @return the property name if it fails a check, otherwise null (meaning everything is OK)
	 */
	public static String testAnyConditionalOnProperty(Type type, AotOptions aotOptions) {
		// Examples:
		//	found COP on org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
		//	copDescriptor: @COP(names=[spring.flyway.enabled],matchIfMissing=true)
		//  found COP on org.springframework.boot.autoconfigure.h2.H2ConsoleAutoConfiguration
		//	copDescriptor @COP(names=[spring.h2.console.enabled],havingValue=true,matchIfMissing=false)
		AnnotationNode annotation = type.getAnnotation(AtConditionalOnProperty);
		if (annotation != null) {
			ConditionalOnPropertyDescriptor copDescriptor = unpackConditionalOnPropertyAnnotation(annotation, aotOptions);
			Map<String,String> activeProperties = type.getTypeSystem().getActiveProperties();
			return copDescriptor.test(activeProperties, aotOptions);
		}
		return null;
	}
	
	// @ConditionalOnEnabledHealthIndicator("cassandra")
	private static ConditionalOnEnabledHealthIndicatorDescriptor unpackConditionalOnEnabledHealthIndicatorAnnotation(AnnotationNode annotation) {
		String value = fetchAnnotationAttributeValueAsString(annotation, "value");
		return new ConditionalOnEnabledHealthIndicatorDescriptor(value);
	}

	private static Type fetchAnnotationAttributeValueAsClass(TypeSystem typeSystem, AnnotationNode annotation, String attributeName) {
		List<Object> values = annotation.values;
		for (int i=0;i<values.size();i+=2) {
			String n = (String)values.get(i);
			if (n.equals(attributeName)) {
				String cn = ((org.objectweb.asm.Type)values.get(i+1)).getClassName();
				return typeSystem.resolveDotted(cn,true);
			}
		}
		return null;
	}

	private static ConditionalOnAvailableEndpointDescriptor unpackConditionalOnAvailableEndpointAnnotation(TypeSystem typeSystem, AnnotationNode annotation) {
		Type endpointClass = fetchAnnotationAttributeValueAsClass(typeSystem, annotation, "endpoint");
		AnnotationNode endpointAnnotation = endpointClass.getAnnotation(AtEndpoint);
		if (endpointAnnotation == null) {
			logger.debug("Couldn't find @Endpoint on "+endpointClass.getName());
			// We are seeing meta usage of the endpoint annotation
			endpointAnnotation = endpointClass.getAnnotationMetaAnnotatedWith(AtEndpoint);
			if (endpointAnnotation == null) {
				logger.debug("Couldn't find meta usage of @Endpoint on "+endpointClass.getName());
			}
		}
		String endpointId = fetchAnnotationAttributeValueAsString(endpointAnnotation,"id");
		// TODO pull in enableByDefault option from endpointAnnotation
		return new ConditionalOnAvailableEndpointDescriptor(endpointId);
	}

	
	private static String fetchAnnotationAttributeValueAsString(AnnotationNode annotation, String attributeName) {
		List<Object> values = annotation.values;
		for (int i=0;i<values.size();i+=2) {
			String n = (String)values.get(i);
			if (n.equals(attributeName)) {
				return (String)values.get(i+1);
			}
		}
		return null;
	}
	
	private static ConditionalOnEnabledMetricsExportDescriptor unpackConditionalOnEnabledMetricsExportDescriptor(AnnotationNode annotation) {
		List<Object> values = annotation.values;
		String name = null;
		for (int i=0;i<values.size();i++) {
			String attributeName = (String)values.get(i);
			if (attributeName.equals("name") || attributeName.equals("value")) {
				name = (String)values.get(++i);
			}
		}
		return new ConditionalOnEnabledMetricsExportDescriptor(name);
	}

	static class ConditionalOnEnabledMetricsExportDescriptor implements TestableDescriptor {

		private String metricsExporter;

		public ConditionalOnEnabledMetricsExportDescriptor(String metricsExporter) {
			this.metricsExporter = metricsExporter;
		}

		public String test(Map<String, String> properties, AotOptions aotOptions) {
			String key = "management.metrics.export."+metricsExporter+".enabled";
			if (!aotOptions.buildTimeCheckableProperty(key)) {
				return null;
			}
			String value = properties.get(key);
			if (value!=null && !value.equalsIgnoreCase("true")) {
				return null;
			}
			String defaultKey = "management.metrics.export.defaults.enabled";
			if (!aotOptions.buildTimeCheckableProperty(defaultKey)) {
				return null;
			}
			String defaultValue = properties.get(defaultKey);
			if (defaultValue!=null && !defaultValue.equalsIgnoreCase("true")) {
				return null;
			}
			return "neither "+key+" nor "+defaultKey+" are true";
		}

		public String toString() {
			return "@COEME("+metricsExporter+")";
		}

	}

	
	private static ConditionalOnPropertyDescriptor unpackConditionalOnPropertyAnnotation(AnnotationNode annotation, AotOptions aotOptions) {
		List<Object> values = annotation.values;
		List<String> names = new ArrayList<>();
		String prefix = null;
		String havingValue = null;
		boolean matchIfMissing = false;;
		for (int i=0;i<values.size();i++) {
			String attributeName = (String)values.get(i);
			if (attributeName.equals("name") || attributeName.equals("value")) {
				names.addAll((List<String>)values.get(++i));
			} else if (attributeName.equals("prefix")) {
				prefix = (String)values.get(++i);
			} else if (attributeName.equals("matchIfMissing")) {
				matchIfMissing = (Boolean)values.get(++i);
			} else if (attributeName.equals("havingValue")) {
				havingValue = (String)values.get(++i);
			} else {
				throw new IllegalStateException("Not expecting attribute named '"+attributeName+"' in ConditionalOnProperty annotation");
			}
		}
		if (prefix != null) {
			if (!prefix.endsWith(".")) {
				prefix = prefix + ".";
			}
			for (int i=0;i<names.size();i++) {
				names.set(i, prefix+names.get(i));
			}
		}
		return new ConditionalOnPropertyDescriptor(names, havingValue, matchIfMissing, aotOptions);
	}
	
	
	// Examples:
	// @ConditionalOnEnabledMetricsExport("simple")
	public static String testAnyConditionalOnEnabledMetricsExport(Type type, AotOptions aotOptions) {
		AnnotationNode annotation = type.getAnnotation(AtConditionalOnEnabledMetricsExport);
		if (annotation != null) {
			ConditionalOnEnabledMetricsExportDescriptor coemedDescriptor = unpackConditionalOnEnabledMetricsExportDescriptor(annotation);
			Map<String,String> activeProperties = type.getTypeSystem().getActiveProperties();
			return coemedDescriptor.test(activeProperties, aotOptions);
		}
		return null;
	}

	// Example:
	// @ConditionalOnAvailableEndpoint(endpoint = FlywayEndpoint.class) public class FlywayEndpointAutoConfiguration {
	// @Endpoint(id = "flyway") public class FlywayEndpoint {
	public static String testAnyConditionalOnAvailableEndpoint(Type type, AotOptions aotOptions) {
		AnnotationNode annotation = type.getAnnotation(AtConditionalOnAvailableEndpoint);
		if (annotation != null) {
			ConditionalOnAvailableEndpointDescriptor coaeDescriptor = unpackConditionalOnAvailableEndpointAnnotation(type.getTypeSystem(), annotation);
			Map<String,String> activeProperties = type.getTypeSystem().getActiveProperties();
			return coaeDescriptor.test(activeProperties, aotOptions);
		}
		return null;
	}
	
	// Example:
	// @ConditionalOnEnabledHealthIndicator("diskspace")
	// @AutoConfigureBefore(HealthContributorAutoConfiguration.class)
	// @EnableConfigurationProperties(DiskSpaceHealthIndicatorProperties.class)
	// public class DiskSpaceHealthContributorAutoConfiguration {
	public static String testAnyConditionalOnEnabledHealthIndicator(Type type, AotOptions aotOptions) {
		AnnotationNode annotation = type.getAnnotation(AtConditionalOnEnabledHealthIndicator);
		if (annotation != null) {
			ConditionalOnEnabledHealthIndicatorDescriptor coehiDescriptor = unpackConditionalOnEnabledHealthIndicatorAnnotation(annotation);
			Map<String,String> activeProperties = type.getTypeSystem().getActiveProperties();
			return coehiDescriptor.test(activeProperties, aotOptions);
		}
		return null;
	}


	interface TestableDescriptor {
		/**
		 * @return null if test is successful, otherwise a string explanation of why it failed
		 */
		String test(Map<String, String> properties, AotOptions aotOptions);
	}

	static class ConditionalOnEnabledHealthIndicatorDescriptor implements TestableDescriptor {

		private String value;

		public ConditionalOnEnabledHealthIndicatorDescriptor(String value) {
			this.value = value;
		}

		@Override
		public String test(Map<String, String> properties, AotOptions aotOptions) {
			String key = "management.health."+value+".enabled";
			if (!aotOptions.buildTimeCheckableProperty(key)) {
				return null;
			}
			String value = properties.get(key);
			// This check is successful if the property is set to true, otherwise
			// either the property wasn't set or it wasn't set to a truthy value
			if (value!=null && value.equalsIgnoreCase("true")) {
				return null;
			}
			String defaultKey = "management.health.defaults.enabled";
			if (!aotOptions.buildTimeCheckableProperty(defaultKey)) {
				return null;
			}
			String defaultValue = properties.get(defaultKey);
			if (defaultValue!=null && defaultValue.equalsIgnoreCase("true")) {
				return null;
			}
			return "neither "+key+" nor "+defaultKey+" are set to true";
		}

		public String toString() {
			return "@COEHI("+value+")";
		}

	}
	
	static class ConditionalOnAvailableEndpointDescriptor implements TestableDescriptor {

		private String endpointId;

		public ConditionalOnAvailableEndpointDescriptor(String endpointId) {
			this.endpointId = endpointId;
		}

		// https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html
		// Crude first pass - ignore JMX exposure and only consider web exposure include (not exclude)
		public String test(Map<String, String> properties, AotOptions aotOptions) {
			String key = "management.endpoints.web.exposure.include";
			if (!aotOptions.buildTimeCheckableProperty(key)) {
				return null;
			}
			String webExposedEndpoints = properties.get(key);
			if (webExposedEndpoints==null) {
				webExposedEndpoints="";
			}
			webExposedEndpoints+=(webExposedEndpoints.length()==0?"info,health":",info,health");
			String[] exposedEndpointIds = webExposedEndpoints.split(",");
			for (String exposedEndpointId: exposedEndpointIds) {
				if (exposedEndpointId.equals(endpointId)) {
					logger.debug("COAE check: endpoint "+endpointId+" *is* exposed via management.endpoints.web.exposed.include");
					return null;
				}
			}
			logger.debug("COAE check: endpoint "+endpointId+" *is not* exposed via management.endpoints.web.exposed.include");
			return "management.endpoints.web.exposed.include="+webExposedEndpoints+" does not contain endpoint "+endpointId;
		}

		public String toString() {
			return "@COAE(id="+endpointId+")";
		}

	}

	public static class ConditionalOnPropertyDescriptor implements TestableDescriptor {

		private List<String> propertyNames;
		private String havingValue;
		private boolean matchIfMissing;
		private AotOptions aotOptions;

		public ConditionalOnPropertyDescriptor(List<String> names, String havingValue, boolean matchIfMissing, AotOptions aotOptions) {
			this.propertyNames = names;
			this.havingValue = havingValue;
			this.matchIfMissing = matchIfMissing;
			this.aotOptions = aotOptions;
		}
		
		public String test(Map<String, String> properties, AotOptions aotOptions) {
			for (String name: propertyNames) {
				if (!aotOptions.buildTimeCheckableProperty(name)) {
					return null;
				}
				String definedValue = properties.get(name);
				if ((havingValue == null && !(definedValue==null || definedValue.toLowerCase().equals("false"))) ||
					(havingValue != null && definedValue!=null && definedValue.toLowerCase().equals(havingValue.toLowerCase()))) {
					// all is well!
				} else if (matchIfMissing && definedValue == null) {
					if (aotOptions.isBuildTimePropertiesMatchIfMissing()) {
						// all is well
					} else {
						return name+(havingValue==null?"":"="+havingValue)+" property check failed because configuration indicated matchIfMissing should be ignored";
					}
				} else {
					return name+(havingValue==null?"":"="+havingValue)+" property check failed "+(definedValue==null?"":" against the discovered value "+definedValue);
				}
			}
			return null;
		}

		public String toString() {
			return "@COP(names="+propertyNames+","+(havingValue==null?"":"havingValue="+havingValue+",")+"matchIfMissing="+matchIfMissing;
		}
		
	}

}

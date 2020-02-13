//package org.springframework.graal.extension;
//
//import java.util.Set;
//
//import org.springframework.graal.type.Access;
//
//public class TypeHint {
//
//	private String typename;
//	private Class<?> clazz;
//	
//	private boolean follow;
//
//	private Set<Access> accessRequired;
//
//	private TypeHint(Class<?> clazz) {
//		this.clazz = clazz;
//	}
//
//	private TypeHint(String typename) {
//		this.typename = typename;
//	}
//	
//	// --- factory methods
//
//	public static TypeHint forConfigurationClass(Class<?> configurationClass) {
//		return forClass(configurationClass).follow();
//	}
//
//	public static TypeHint forConfigurationName(String configurationClassName) {
//		return forName(configurationClassName).follow();
//	}
//	
//	public static TypeHint forClass(Class<?> clazz) {
//		return new TypeHint(clazz);
//	}
//
//	public static TypeHint forName(String typename) {
//		return new TypeHint(typename);
//	}
//	
//	// --- 
//	
//	public TypeHint follow() {
//		this.follow = true;
//		return this;
//	}
//
//	public TypeHint accessRequired(Set<Access> accessRequired) {
//		this.accessRequired = accessRequired;
//		return this;
//	}
//	
//}
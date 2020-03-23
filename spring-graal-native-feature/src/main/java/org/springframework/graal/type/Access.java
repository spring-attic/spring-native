//package org.springframework.graal.type;
//
//import java.util.LinkedHashSet;
//import java.util.Set;
//
//public enum Access {
//	RESOURCE;
//
//	public static Set<Access> from(AccessRequired ar) {
//		Set<Access> requiredAccess = new LinkedHashSet<>();
//		if (ar.isResourceAccess()) {
//			requiredAccess.add(Access.RESOURCE);
//		}
//		return requiredAccess;
//	}
//}

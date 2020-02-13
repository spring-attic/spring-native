package org.springframework.graal.type;

import java.util.LinkedHashMap;
import java.util.Map;

public class CompilationHint {
	private String targetType;
	private Map<String, Integer> specificTypes = new LinkedHashMap<>();

	public boolean follow = false;
	public boolean skipIfTypesMissing = false;
	
	public CompilationHint() {
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CompilationHint");
		if (targetType != null) {
			sb.append(" for ").append(targetType);
		}
		sb.append(":");
		sb.append(specificTypes);
		return sb.toString();
	}
	
	/*
	public CompilationHint(boolean skipIfTypesMissing, boolean follow) {
		this(skipIfTypesMissing, follow, new String[] {});
	}
	
	public CompilationHint(boolean skipIfTypesMissing, boolean follow, String[] specificTypes) {
		this.skipIfTypesMissing = skipIfTypesMissing;
		this.follow = follow;
		if (specificTypes != null) {
			this.specificTypes = new LinkedHashMap<>();
			for (String specificType: specificTypes) {
				TypeKind access = TypeKind.UNRECOGNIZED;
				StringTokenizer t = new StringTokenizer(specificType,":");
				String type = t.nextToken(); // the type name
				if (t.hasMoreTokens()) { // possible access specified otherwise default to ALL
					access = TypeKind.valueOf(t.nextToken());
				}
				this.specificTypes.put(type, access);
			}
		} else {
			this.specificTypes = Collections.emptyMap();
		}
	}
	*/

	public void setTargetType(String targetTypename) {
		this.targetType = targetTypename;
	}

	public String getTargetType() {
		return targetType;
	}
	
	public Map<String, Integer> getDependantTypes() {
		return specificTypes;
	}

	public void addDependantType(String className, Integer accessBits) {
		specificTypes.put(className, accessBits);
	}

	public void setAbortIfTypesMissing(Boolean b) {
		skipIfTypesMissing = b;
	}

	public boolean isAbortIfTypesMissing() {
		return skipIfTypesMissing;
	}

	public void setFollow(Boolean b) {
		follow = b;
	}
	
}
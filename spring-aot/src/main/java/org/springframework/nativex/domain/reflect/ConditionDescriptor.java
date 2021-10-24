package org.springframework.nativex.domain.reflect;

import java.util.Objects;

public class ConditionDescriptor {

	private String typeReachable;


	public ConditionDescriptor(String typeReachable) {
		setTypeReachable(typeReachable);
	}

	public String getTypeReachable() {
		return typeReachable;
	}

	public void setTypeReachable(String typeReachable) {
		if (!Object.class.getName().equals(typeReachable)) {
			this.typeReachable = typeReachable;
		}
	}

	public String toJsonString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"typeReachable\":").append("\""+typeReachable+"\"");
		sb.append("}");
		return sb.toString();
	}

	@Override
	public String toString() {
		return typeReachable;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConditionDescriptor that = (ConditionDescriptor) o;
		return Objects.equals(typeReachable, that.typeReachable);
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeReachable);
	}
}
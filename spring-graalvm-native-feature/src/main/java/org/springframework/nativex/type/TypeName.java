package org.springframework.nativex.type;

import java.util.Objects;

/**
 * @author Brian Clozel
 */
public class TypeName {

	private final String className;

	private final int dimensions;

	private TypeName(String className, int dimensions) {
		this.className = className;
		this.dimensions = dimensions;
	}

	public String toClassName() {
		return appendDimensions(this.className);
	}

	public String toSimpleName() {
		String simpleName = this.className.substring(this.className.lastIndexOf('.') + 1);
		return appendDimensions(simpleName);
	}

	public String toShortName() {
		String dname = toClassName();
		StringBuilder s = new StringBuilder();
		boolean hasDot = dname.contains(".");
		while (dname.contains(".")) {
			s.append(dname.charAt(0));
			dname = dname.substring(dname.indexOf(".")+1);
		}
		if (hasDot) {
			s.append(".");
		}
		s.append(dname);
		return s.toString();
	}

	private String appendDimensions(String name) {
		if (this.dimensions == 0) {
			return name;
		}
		else {
			StringBuilder builder = new StringBuilder(name);
			for (int i = 0; i < this.dimensions; i++) {
				builder.append("[]");
			}
			return builder.toString();
		}
	}

	public String toSlashName() {
		return appendDimensions(this.className.replace('.', '/'));
	}
	
	public String toTypeSignature() {
		StringBuilder typeSignature = new StringBuilder();
		for (int i = 0; i < this.dimensions; i++) {
			typeSignature.append('[');
		}
		return typeSignature
				.append('L')
				.append(this.className.replace(".", "/"))
				.append(';')
				.toString();
	}

	public String getPackageName() {
		return this.className.substring(0, this.className.lastIndexOf('.'));
	}

	public static TypeName fromClassName(String className) {
		int dimensions = 0;
		while (className.endsWith("[]")) {
			dimensions++;
			className = className.substring(0, className.length() - 2);
		}
		return new TypeName(className, dimensions);
	}

	public static TypeName fromSlashName(String slashName) {
		int dimensions = 0;
		while (slashName.endsWith("[]")) {
			dimensions++;
			slashName = slashName.substring(0, slashName.length() - 2);
		}
		return new TypeName(slashName.replace('/', '.'), dimensions);
	}

	public static TypeName fromTypeSignature(String typeSignature) {
		int dimensions = 0;
		int pos = 0;
		while (typeSignature.charAt(pos) == '[') {
			pos++;
			dimensions++;
		}
		String className = typeSignature.substring(pos + 1, typeSignature.length() - 1).replace('/', '.');
		return new TypeName(className, dimensions);
	}

	@Override
	public String toString() {
		return this.toClassName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TypeName typeName = (TypeName) o;
		return dimensions == typeName.dimensions && className.equals(typeName.className);
	}

	@Override
	public int hashCode() {
		return Objects.hash(className, dimensions);
	}
}

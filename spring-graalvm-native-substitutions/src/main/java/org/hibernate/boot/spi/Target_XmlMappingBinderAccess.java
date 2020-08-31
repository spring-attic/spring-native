package org.hibernate.boot.spi;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.hibernate.boot.archive.spi.InputStreamAccess;
import org.hibernate.boot.jaxb.internal.MappingBinder;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.service.ServiceRegistry;

import org.springframework.graalvm.substitutions.OnlyIfPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.hibernate.boot.spi.XmlMappingBinderAccess", onlyWith = { OnlyIfPresent.class, RemoveXmlSupport.class })
final class Target_XmlMappingBinderAccess {

	@Substitute
	public Target_XmlMappingBinderAccess(ServiceRegistry serviceRegistry) {
	}

	@Substitute
	public MappingBinder getMappingBinder() {
		return null;
	}

	@Substitute
	public Binding bind(String resource) {
		throw new UnsupportedOperationException("Hibernate XML support disabled when spring.xml.ignore flag is enabled");
	}

	@Substitute
	public Binding bind(File file) {
		throw new UnsupportedOperationException("Hibernate XML support disabled when spring.xml.ignore flag is enabled");
	}

	@Substitute
	public Binding bind(InputStreamAccess xmlInputStreamAccess) {
		throw new UnsupportedOperationException("Hibernate XML support disabled when spring.xml.ignore flag is enabled");
	}

	@Substitute
	public Binding bind(InputStream xmlInputStream) {
		throw new UnsupportedOperationException("Hibernate XML support disabled when spring.xml.ignore flag is enabled");
	}

	@Substitute
	public Binding bind(URL url) {
		throw new UnsupportedOperationException("Hibernate XML support disabled when spring.xml.ignore flag is enabled");
	}
}

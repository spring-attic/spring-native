package org.springframework.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;

@TargetClass(className = "org.springframework.util.DefaultPropertiesPersister", onlyWith = { OnlyPresent.class, RemoveXmlSupport.class })
final class Target_DefaultPropertiesPersister {

	@Substitute
	public void loadFromXml(Properties props, InputStream is) throws IOException {
		throw new UnsupportedOperationException("XML support disabled");
	}

	@Substitute
	public void storeToXml(Properties props, OutputStream os, String header) throws IOException {
		throw new UnsupportedOperationException("XML support disabled");
	}

	@Substitute
	public void storeToXml(Properties props, OutputStream os, String header, String encoding) throws IOException {
		throw new UnsupportedOperationException("XML support disabled");
	}
}

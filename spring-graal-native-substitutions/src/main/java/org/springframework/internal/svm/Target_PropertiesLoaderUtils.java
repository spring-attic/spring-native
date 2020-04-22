package org.springframework.internal.svm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.core.io.Resource;

/**
 * Substitution for PropertiesLoaderUtils in order to avoid XML parsers which increase artificially the image size and and the memory consumption.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://github.com/oracle/graal/issues/2327">oracle/graal#2327</a>
 */
@TargetClass(className="org.springframework.core.io.support.PropertiesLoaderUtils", onlyWith = OnlyPresent.class)
public final class Target_PropertiesLoaderUtils {

	@Substitute
	public static void fillProperties(Properties props, Resource resource) throws IOException {
		try (InputStream is = resource.getInputStream()) {
			props.load(is);
		}
	}

}

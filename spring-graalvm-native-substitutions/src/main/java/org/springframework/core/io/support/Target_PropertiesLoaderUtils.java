package org.springframework.core.io.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.core.io.Resource;
import org.springframework.graalvm.substitutions.OnlyPresent;
import org.springframework.graalvm.substitutions.RemoveXmlSupport;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

/**
 * Substitution for PropertiesLoaderUtils in order to avoid XML parsers which increase artificially the image size and and the memory consumption.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://github.com/oracle/graal/issues/2327">oracle/graal#2327</a>
 */
@TargetClass(className="org.springframework.core.io.support.PropertiesLoaderUtils", onlyWith = { OnlyPresent.class, RemoveXmlSupport.class })
public final class Target_PropertiesLoaderUtils {

	@Substitute
	public static void fillProperties(Properties props, Resource resource) throws IOException {
		try (InputStream is = resource.getInputStream()) {
			props.load(is);
		}
	}

	@Substitute
	public static Properties loadAllProperties(String resourceName, @Nullable ClassLoader classLoader) throws IOException {
		Assert.notNull(resourceName, "Resource name must not be null");
		ClassLoader classLoaderToUse = classLoader;
		if (classLoaderToUse == null) {
			classLoaderToUse = ClassUtils.getDefaultClassLoader();
		}
		Enumeration<URL> urls = (classLoaderToUse != null ? classLoaderToUse.getResources(resourceName) :
				ClassLoader.getSystemResources(resourceName));
		Properties props = new Properties();
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			URLConnection con = url.openConnection();
			ResourceUtils.useCachesIfNecessary(con);
			try (InputStream is = con.getInputStream()) {
				props.load(is);
			}
		}
		return props;
	}

}

package org.springframework.nativex.buildtools;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import org.springframework.nativex.type.TypeSystem;
import org.springframework.util.StringUtils;

/**
 * JUnit extension that creates a test-level {@link TypeSystem} using the test class classloader
 * and injects it as a test method parameter.
 * 
 * @author Brian Clozel
 *
 * @see {@code org.springframework.boot.testsupport.classpath.ModifiedClassPathClassLoader}
 */
public class TypeSystemExtension implements BeforeAllCallback, ParameterResolver {

	private static final Pattern INTELLIJ_CLASSPATH_JAR_PATTERN = Pattern.compile(".*classpath(\\d+)?\\.jar");

	private TypeSystem typeSystem;

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		List<String> classpath = getClasspath(extensionContext.getRequiredTestClass().getClassLoader())
				.collect(Collectors.toList());
		this.typeSystem = new TypeSystem(classpath);
	}


	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		return parameterContext.getParameter().getType().isAssignableFrom(TypeSystem.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		return this.typeSystem;
	}

	private static Stream<String> getClasspath(ClassLoader classLoader) {
		return getClasspathEntries(classLoader).flatMap(TypeSystemExtension::extractEntry)
				.map(URL::getPath);
	}

	private static Stream<URL> getClasspathEntries(ClassLoader classLoader) {
		if (classLoader instanceof URLClassLoader) {
			return Stream.of(((URLClassLoader) classLoader).getURLs());
		}
		return Stream.of(ManagementFactory.getRuntimeMXBean().getClassPath().split(File.pathSeparator))
				.map(TypeSystemExtension::toURL);
	}

	private static URL toURL(String entry) {
		try {
			return new File(entry).toURI().toURL();
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	private static Stream<URL> extractEntry(URL url) {
		if (isManifestOnlyJar(url)) {
			return extractUrlsFromManifestClassPath(url).stream();
		}
		else {
			return Stream.of(url);
		}
	}

	private static boolean isManifestOnlyJar(URL url) {
		return isSurefireBooterJar(url) || isShortenedIntelliJJar(url);
	}

	private static boolean isSurefireBooterJar(URL url) {
		return url.getPath().contains("surefirebooter");
	}

	private static boolean isShortenedIntelliJJar(URL url) {
		String urlPath = url.getPath();
		boolean isCandidate = INTELLIJ_CLASSPATH_JAR_PATTERN.matcher(urlPath).matches();
		if (isCandidate) {
			try {
				Attributes attributes = getManifestMainAttributesFromUrl(url);
				String createdBy = attributes.getValue("Created-By");
				return createdBy != null && createdBy.contains("IntelliJ");
			}
			catch (Exception ex) {
			}
		}
		return false;
	}

	private static Attributes getManifestMainAttributesFromUrl(URL url) throws Exception {
		try (JarFile jarFile = new JarFile(new File(url.toURI()))) {
			return jarFile.getManifest().getMainAttributes();
		}
	}

	private static List<URL> extractUrlsFromManifestClassPath(URL booterJar) {
		List<URL> urls = new ArrayList<>();
		try {
			for (String entry : getClassPath(booterJar)) {
				urls.add(new URL(entry));
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return urls;
	}

	private static String[] getClassPath(URL booterJar) throws Exception {
		Attributes attributes = getManifestMainAttributesFromUrl(booterJar);
		return StringUtils.delimitedListToStringArray(attributes.getValue(Attributes.Name.CLASS_PATH), " ");
	}

}

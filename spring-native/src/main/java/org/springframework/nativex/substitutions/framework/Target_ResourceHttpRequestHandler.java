package org.springframework.nativex.substitutions.framework;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.util.StringValueResolver;
import org.springframework.web.context.support.ServletContextResource;

// Workaround for https://github.com/spring-projects-experimental/spring-native/issues/1174 with GraalVM 21.2.0 (works fine with GraalVM 21.3.0)
@TargetClass(className = "org.springframework.web.servlet.resource.ResourceHttpRequestHandler", onlyWith = OnlyIfPresent.class)
final class Target_ResourceHttpRequestHandler {

	@Alias
	private List<String> locationValues;

	@Alias
	private List<Resource> locationResources;

	@Alias
	private List<Resource> locationsToUse;

	@Alias
	private Map<Resource, Charset> locationCharsets;

	@Alias
	private StringValueResolver embeddedValueResolver;

	@Substitute
	private void resolveResourceLocations() {
		List<Resource> result = new ArrayList<>();
		if (!this.locationValues.isEmpty()) {
			ApplicationContext applicationContext = ((Target_ApplicationObjectSupport)(Object)this).obtainApplicationContext();
			for (String location : this.locationValues) {
				if (this.embeddedValueResolver != null) {
					String resolvedLocation = this.embeddedValueResolver.resolveStringValue(location);
					if (resolvedLocation == null) {
						throw new IllegalArgumentException("Location resolved to null: " + location);
					}
					location = resolvedLocation;
				}
				Charset charset = null;
				location = location.trim();
				if (location.startsWith("[charset=")) {
					int endIndex = location.indexOf(']', "[charset=".length());
					if (endIndex == -1) {
						throw new IllegalArgumentException("Invalid charset syntax in location: " + location);
					}
					String value = location.substring("[charset=".length(), endIndex);
					charset = Charset.forName(value);
					location = location.substring(endIndex + 1);
				}
				Resource resource = applicationContext.getResource(location);
				if (location.equals("/") && !(resource instanceof ServletContextResource)) {
					throw new IllegalStateException(
							"The String-based location \"/\" should be relative to the web application root " +
									"but resolved to a Resource of type: " + resource.getClass() + ". " +
									"If this is intentional, please pass it as a pre-configured Resource via setLocations.");
				}
				result.add(resource);
				if (charset != null) {
					if (!(resource instanceof UrlResource)) {
						throw new IllegalArgumentException("Unexpected charset for non-UrlResource: " + resource);
					}
					this.locationCharsets.put(resource, charset);
				}
			}
		}

		result.addAll(this.locationResources);

		this.locationsToUse.clear();
		this.locationsToUse.addAll(result);
	}

}

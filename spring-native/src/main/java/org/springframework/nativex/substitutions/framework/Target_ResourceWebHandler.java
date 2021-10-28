package org.springframework.nativex.substitutions.framework;

import java.util.ArrayList;
import java.util.List;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.nativex.substitutions.OnlyIfPresent;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

// Workaround for https://github.com/spring-projects-experimental/spring-native/issues/1174 with GraalVM 21.2.0 (works fine with GraalVM 21.3.0)
@TargetClass(className = "org.springframework.web.reactive.resource.ResourceWebHandler", onlyWith = OnlyIfPresent.class)
final class Target_ResourceWebHandler {

	@Alias
	private List<String> locationValues;

	@Alias
	private  List<Resource> locationResources;

	@Alias
	private List<Resource> locationsToUse;

	@Alias
	private ResourceLoader resourceLoader;

	@Substitute
	private void resolveResourceLocations() {
		List<Resource> result = new ArrayList<>(this.locationResources);

		if (!this.locationValues.isEmpty()) {
			Assert.notNull(this.resourceLoader,
					"ResourceLoader is required when \"locationValues\" are configured.");
			Assert.isTrue(CollectionUtils.isEmpty(this.locationResources), "Please set " +
					"either Resource-based \"locations\" or String-based \"locationValues\", but not both.");
			for (String location : this.locationValues) {
				result.add(this.resourceLoader.getResource(location));
			}
		}

		this.locationsToUse.clear();
		this.locationsToUse.addAll(result);
	}
}

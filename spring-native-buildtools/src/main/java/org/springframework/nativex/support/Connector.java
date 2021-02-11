package org.springframework.nativex.support;

import java.io.InputStream;
import java.util.List;

import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;
import org.springframework.nativex.type.Type;

public interface Connector {

	void addResourcesDescriptor(ResourcesDescriptor resourcesDescriptor);

	void addReflectionDescriptor(ReflectionDescriptor filteredReflectionDescriptor);

	void registerResource(String resourceName, InputStream inputStream);

	void addResource(String pattern, boolean isBundle);

	void initializeAtRunTime(String[] typenames);

	void initializeAtBuildTime(String[] typenames);

	void initializeAtBuildTimePackages(String[] packageNames);

	void initializeAtRunTimePackages(String[] packageNames);

	void addProxy(List<String> interfaceNames);

	void initializeAtRunTime(List<Type> types);

	void addClassDescriptor(ClassDescriptor classDescriptor);

}

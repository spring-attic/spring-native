/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.nativex;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.nativex.domain.resources.ResourcesDescriptor;
import org.springframework.nativex.domain.resources.ResourcesJsonMarshaller;

public class ResourcesTests {

	@Test
	public void testApacheTomcatResources() throws Exception {
		InputStream s = this.getClass().getResourceAsStream("/META-INF/native-image/org.apache.tomcat.embed/tomcat-embed-core/tomcat-resource.json");
		ResourcesDescriptor read = ResourcesJsonMarshaller.read(s);
		Assert.assertTrue(read.getBundles().contains("javax.servlet.LocalStrings"));
		Assert.assertTrue(read.getPatterns().contains("^org/apache/tomcat/.*mbeans-descriptors\\.xml$"));
		Assert.assertTrue(read.getPatterns().contains("^org/apache/catalina/.*mbeans-descriptors\\.xml$"));
		Assert.assertTrue(read.getPatterns().contains("^org/apache/coyote/.*mbeans-descriptors\\.xml$"));
	}

	@Test
	public void testCloudResources() throws Exception {
		InputStream s = this.getClass().getResourceAsStream("/cloud-resource-config.json");
		ResourcesJsonMarshaller.read(s);
	}

}

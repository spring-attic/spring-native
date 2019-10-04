/*
 * Copyright 2019 Contributors
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
package org.springframework.internal.svm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.oracle.svm.core.SubstrateUtil;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import org.springframework.core.io.AbstractFileResolvingResource;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.log.LogAccessor;

/**
 * Workaround for https://github.com/oracle/graal/issues/1683
 * 
 * @author Andy Clement
 */
@TargetClass(className = "org.springframework.core.io.AbstractFileResolvingResource", onlyWith = OnlyPresent.class)
public final class Target_org_springframework_core_io_classpathresource {

	@Substitute
	public long contentLength() throws IOException {
		AbstractFileResolvingResource resource = SubstrateUtil.cast(this, AbstractFileResolvingResource.class);

		InputStream is = resource.getInputStream();
		try {
			long size = 0;
			byte[] buf = new byte[256];
			int read;
			while ((read = is.read(buf)) != -1) {
				size += read;
			}
			return size;
		} finally {
			try {
				is.close();
			} catch (IOException ex) {
				System.err.println("Could not close InputStream for resource: " + resource.getDescription());
			}
		}
	}
}

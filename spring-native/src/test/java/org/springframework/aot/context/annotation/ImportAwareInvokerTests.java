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

package org.springframework.aot.context.annotation;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link ImportAwareInvoker}.
 *
 * @author Stephane Nicoll
 */
class ImportAwareInvokerTests {

	@Test
	void setAnnotationMetadataOnMatchingCandidate() {
		ImportAwareInvoker invoker = new ImportAwareInvoker(Map.of(TestImportAware.class.getName(),
				ImportAwareInvokerTests.class.getName()));
		TestImportAware importAware = new TestImportAware();
		invoker.setAnnotationMetadata(importAware);
		assertThat(importAware.importMetadata).isNotNull();
		assertThat(importAware.importMetadata.getClassName())
				.isEqualTo(ImportAwareInvokerTests.class.getName());
	}

	@Test
	void setAnnotationMetadataOnMatchingCandidateWithNestedClass() {
		ImportAwareInvoker invoker = new ImportAwareInvoker(Map.of(TestImportAware.class.getName(),
				TestImporting.class.getName()));
		TestImportAware importAware = new TestImportAware();
		invoker.setAnnotationMetadata(importAware);
		assertThat(importAware.importMetadata).isNotNull();
		assertThat(importAware.importMetadata.getClassName())
				.isEqualTo(TestImporting.class.getName());
	}

	@Test
	void setAnnotationMetadataOnNonMatchingCandidate() {
		ImportAwareInvoker invoker = new ImportAwareInvoker(Map.of(String.class.getName(),
				ImportAwareInvokerTests.class.getName()));
		TestImportAware importAware = new TestImportAware();
		assertThatIllegalArgumentException().isThrownBy(() -> invoker.setAnnotationMetadata(importAware))
				.withMessageContaining("No import information available for '%s'", TestImportAware.class.getName());
	}

	@Test
	void setAnnotationMetadataOnMatchingCandidateWithNoMetadata() {
		ImportAwareInvoker invoker = new ImportAwareInvoker(Map.of(TestImportAware.class.getName(),
				"com.example.invalid.DoesNotExist"));
		TestImportAware importAware = new TestImportAware();
		assertThatIllegalStateException().isThrownBy(() -> invoker.setAnnotationMetadata(importAware))
				.withMessageContaining("Failed to read metadata for 'com.example.invalid.DoesNotExist'");
	}


	static class TestImportAware implements ImportAware {
		private AnnotationMetadata importMetadata;

		@Override
		public void setImportMetadata(AnnotationMetadata importMetadata) {
			this.importMetadata = importMetadata;
		}
	}

	static class TestImporting {

	}

}

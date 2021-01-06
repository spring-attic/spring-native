/*
 * Copyright 2018 the original author or authors.
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
package com.example.data.jdbc;

public final class ModelReport {

	private final String modelName, description, setName;

	public ModelReport(String modelName, String description, String setName) {
		this.modelName = modelName;
		this.description = description;
		this.setName = setName;
	}

	public String getModelName() {
		return this.modelName;
	}

	public String getDescription() {
		return this.description;
	}

	public String getSetName() {
		return this.setName;
	}

	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ModelReport)) return false;
		final ModelReport other = (ModelReport) o;
		final Object this$modelName = this.getModelName();
		final Object other$modelName = other.getModelName();
		if (this$modelName == null ? other$modelName != null : !this$modelName.equals(other$modelName)) return false;
		final Object this$description = this.getDescription();
		final Object other$description = other.getDescription();
		if (this$description == null ? other$description != null : !this$description.equals(other$description))
			return false;
		final Object this$setName = this.getSetName();
		final Object other$setName = other.getSetName();
		if (this$setName == null ? other$setName != null : !this$setName.equals(other$setName)) return false;
		return true;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $modelName = this.getModelName();
		result = result * PRIME + ($modelName == null ? 43 : $modelName.hashCode());
		final Object $description = this.getDescription();
		result = result * PRIME + ($description == null ? 43 : $description.hashCode());
		final Object $setName = this.getSetName();
		result = result * PRIME + ($setName == null ? 43 : $setName.hashCode());
		return result;
	}

	public String toString() {
		return "ModelReport(modelName=" + this.getModelName() + ", description=" + this.getDescription() + ", setName=" + this.getSetName() + ")";
	}

	public ModelReport withModelName(String modelName) {
		return this.modelName == modelName ? this : new ModelReport(modelName, this.description, this.setName);
	}

	public ModelReport withDescription(String description) {
		return this.description == description ? this : new ModelReport(this.modelName, description, this.setName);
	}

	public ModelReport withSetName(String setName) {
		return this.setName == setName ? this : new ModelReport(this.modelName, this.description, setName);
	}
}

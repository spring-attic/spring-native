/*
 * Copyright 2020 the original author or authors.
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

import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.AccessType.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("LEGO_SET")
@AccessType(Type.PROPERTY)
public class LegoSet {

	private @Id int id;
	private String name;
	private @Transient Period minimumAge, maximumAge;

	/**
	 * Since Manuals are part of a {@link LegoSet} and only make sense inside a {@link LegoSet} it is considered part of
	 * the Aggregate.
	 */
	@Column("HANDBUCH_ID")
	private Manual manual;

	// You can build multiple models from one LegoSet
	@MappedCollection(keyColumn = "NAME")
	private final @AccessType(Type.FIELD) Map<String, Model> models;

	@CreatedDate
	private Instant createdAt;

	@LastModifiedDate
	private Instant updatedAt;

	@CreatedBy
	private String createdBy;

	@LastModifiedBy
	private String modifiedBy;

	LegoSet() {
		this.models = new HashMap<>();
	}

	public LegoSet(int id, String name, Period minimumAge, Period maximumAge, Manual manual, Map<String, Model> models) {
		this.id = id;
		this.name = name;
		this.minimumAge = minimumAge;
		this.maximumAge = maximumAge;
		this.manual = manual;
		this.models = models;
	}

	// conversion for custom types currently has to be done through getters/setter + marking the underlying property with
	// @Transient.
	@Column("MIN_AGE")
	public int getIntMinimumAge() {
		return toInt(this.minimumAge);
	}

	public void setIntMinimumAge(int years) {
		minimumAge = toPeriod(years);
	}

	@Column("MAX_AGE")
	public int getIntMaximumAge() {
		return toInt(this.maximumAge);
	}

	public void setIntMaximumAge(int years) {
		maximumAge = toPeriod(years);
	}

	private static int toInt(Period period) {
		return (int) (period == null ? 0 : period.get(ChronoUnit.YEARS));
	}

	private static Period toPeriod(int years) {
		return Period.ofYears(years);
	}

	public void addModel(String name, String description) {

		Model model = new Model(name, description);
		models.put(name, model);
	}

	public LegoSet withModels(Map<String, Model> models) {
		return new LegoSet(id, name, minimumAge, maximumAge, manual, models);
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public Period getMinimumAge() {
		return this.minimumAge;
	}

	public Period getMaximumAge() {
		return this.maximumAge;
	}

	public Manual getManual() {
		return this.manual;
	}

	public Map<String, Model> getModels() {
		return this.models;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMinimumAge(Period minimumAge) {
		this.minimumAge = minimumAge;
	}

	public void setMaximumAge(Period maximumAge) {
		this.maximumAge = maximumAge;
	}

	public void setManual(Manual manual) {
		this.manual = manual;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof LegoSet)) return false;
		final LegoSet other = (LegoSet) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.id != other.id) return false;
		final Object this$name = this.name;
		final Object other$name = other.name;
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$minimumAge = this.minimumAge;
		final Object other$minimumAge = other.minimumAge;
		if (this$minimumAge == null ? other$minimumAge != null : !this$minimumAge.equals(other$minimumAge))
			return false;
		final Object this$maximumAge = this.maximumAge;
		final Object other$maximumAge = other.maximumAge;
		if (this$maximumAge == null ? other$maximumAge != null : !this$maximumAge.equals(other$maximumAge))
			return false;
		final Object this$manual = this.manual;
		final Object other$manual = other.manual;
		if (this$manual == null ? other$manual != null : !this$manual.equals(other$manual)) return false;
		final Object this$models = this.models;
		final Object other$models = other.models;
		if (this$models == null ? other$models != null : !this$models.equals(other$models)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof LegoSet;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.id;
		final Object $name = this.name;
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $minimumAge = this.minimumAge;
		result = result * PRIME + ($minimumAge == null ? 43 : $minimumAge.hashCode());
		final Object $maximumAge = this.maximumAge;
		result = result * PRIME + ($maximumAge == null ? 43 : $maximumAge.hashCode());
		final Object $manual = this.manual;
		result = result * PRIME + ($manual == null ? 43 : $manual.hashCode());
		final Object $models = this.models;
		result = result * PRIME + ($models == null ? 43 : $models.hashCode());
		return result;
	}

	public String toString() {
		return "LegoSet(id=" + this.id +
				", name=" + this.name +
				", minimumAge=" + this.minimumAge +
				", maximumAge=" + this.maximumAge +
				", manual=" + this.manual +
				", models=" + this.models +
				", createdAt=" + this.createdAt +
				", updatedAt=" + this.updatedAt +
				", createdBy=" + this.createdBy +
				", modifiedBy=" + this.modifiedBy +
				")";
	}
}

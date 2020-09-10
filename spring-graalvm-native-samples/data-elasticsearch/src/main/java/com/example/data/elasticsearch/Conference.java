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
package com.example.data.elasticsearch;

import static org.springframework.data.elasticsearch.annotations.FieldType.*;

import java.lang.Object;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Document(indexName = "conference-index")
public class Conference {

	private @Id String id;
	private String name;
	private @Field(type = Date) String date;
	private GeoPoint location;
	private List<String> keywords;

	// do not remove it
	public Conference() {
	}

	// do not remove it - work around for lombok generated constructor for all params
	public Conference(String id, String name, String date, GeoPoint location, List<String> keywords) {

		this.id = id;
		this.name = name;
		this.date = date;
		this.location = location;
		this.keywords = keywords;
	}

	public static ConferenceBuilder builder() {
		return new ConferenceBuilder();
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getDate() {
		return this.date;
	}

	public GeoPoint getLocation() {
		return this.location;
	}

	public List<String> getKeywords() {
		return this.keywords;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof Conference)) return false;
		final Conference other = (Conference) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$id = this.id;
		final Object other$id = other.id;
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$name = this.name;
		final Object other$name = other.name;
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$date = this.date;
		final Object other$date = other.date;
		if (this$date == null ? other$date != null : !this$date.equals(other$date)) return false;
		final Object this$location = this.location;
		final Object other$location = other.location;
		if (this$location == null ? other$location != null : !this$location.equals(other$location)) return false;
		final Object this$keywords = this.keywords;
		final Object other$keywords = other.keywords;
		if (this$keywords == null ? other$keywords != null : !this$keywords.equals(other$keywords)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof Conference;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.id;
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $name = this.name;
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $date = this.date;
		result = result * PRIME + ($date == null ? 43 : $date.hashCode());
		final Object $location = this.location;
		result = result * PRIME + ($location == null ? 43 : $location.hashCode());
		final Object $keywords = this.keywords;
		result = result * PRIME + ($keywords == null ? 43 : $keywords.hashCode());
		return result;
	}

	public String toString() {
		return "Conference(id=" + this.id + ", name=" + this.name + ", date=" + this.date + ", location=" + this.location + ", keywords=" + this.keywords + ")";
	}

	public static class ConferenceBuilder {

		private String id;
		private String name;
		private String date;
		private GeoPoint location;
		private List<String> keywords;

		ConferenceBuilder() {
		}

		public Conference.ConferenceBuilder id(String id) {
			this.id = id;
			return this;
		}

		public Conference.ConferenceBuilder name(String name) {
			this.name = name;
			return this;
		}

		public Conference.ConferenceBuilder date(String date) {
			this.date = date;
			return this;
		}

		public Conference.ConferenceBuilder location(GeoPoint location) {
			this.location = location;
			return this;
		}

		public Conference.ConferenceBuilder keywords(List<String> keywords) {
			this.keywords = keywords;
			return this;
		}

		public Conference build() {
			return new Conference(id, name, date, location, keywords);
		}

		public String toString() {
			return "Conference.ConferenceBuilder(id=" + this.id + ", name=" + this.name + ", date=" + this.date + ", location=" + this.location + ", keywords=" + this.keywords + ")";
		}
	}
}

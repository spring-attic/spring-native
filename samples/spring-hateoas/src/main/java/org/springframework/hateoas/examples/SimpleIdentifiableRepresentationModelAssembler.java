/*
 * Copyright 2017-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.examples;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.lang.reflect.Field;

import org.springframework.core.GenericTypeResolver;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link SimpleRepresentationModelAssembler} that mixes together a Spring web controller and a
 * {@link LinkRelationProvider} to build links upon a certain strategy.
 *
 * @author Greg Turnquist
 */
public class SimpleIdentifiableRepresentationModelAssembler<T> implements SimpleRepresentationModelAssembler<T> {

	/**
	 * The Spring MVC class for the object from which links will be built.
	 */
	private final Class<?> controllerClass;
	/**
	 * A {@link LinkRelationProvider} to look up names of links as options for resource paths.
	 */
	private final LinkRelationProvider relProvider;

	/**
	 * A {@link Class} depicting the object's type.
	 */
	private final Class<?> resourceType;

	/**
	 * Default base path as empty.
	 */
	private String basePath = "";

	/**
	 * Default a assembler based on Spring MVC controller, resource type, and {@link LinkRelationProvider}. With this
	 * combination of information, resources can be defined.
	 *
	 * @param controllerClass - Spring MVC controller to base links off of
	 * @param relProvider
	 * @see #setBasePath(String) to adjust base path to something like "/api"/
	 */
	public SimpleIdentifiableRepresentationModelAssembler(Class<?> controllerClass, LinkRelationProvider relProvider) {

		this.controllerClass = controllerClass;
		this.relProvider = relProvider;

		// Find the "T" type contained in "T extends Identifiable<?>", e.g.
		// SimpleIdentifiableRepresentationModelAssembler<User> -> User
		this.resourceType = GenericTypeResolver.resolveTypeArgument(this.getClass(),
				SimpleIdentifiableRepresentationModelAssembler.class);
	}

	/**
	 * Alternate constructor that falls back to {@link EvoInflectorLinkRelationProvider}.
	 *
	 * @param controllerClass
	 */
	public SimpleIdentifiableRepresentationModelAssembler(Class<?> controllerClass) {
		this(controllerClass, new EvoInflectorLinkRelationProvider());
	}

	/**
	 * Add single item self link based on the object and link back to aggregate root of the {@literal T} domain type using
	 * {@link LinkRelationProvider#getCollectionResourceRelFor(Class)}}.
	 *
	 * @param resource
	 */
	public void addLinks(EntityModel<T> resource) {

		resource.add(getCollectionLinkBuilder().slash(getId(resource)).withSelfRel());
		resource.add(getCollectionLinkBuilder().withRel(this.relProvider.getCollectionResourceRelFor(this.resourceType)));
	}

	private Object getId(EntityModel<T> resource) {

		Field id = ReflectionUtils.findField(this.resourceType, "id");
		ReflectionUtils.makeAccessible(id);

		return ReflectionUtils.getField(id, resource.getContent());
	}

	/**
	 * Add a self link to the aggregate root.
	 *
	 * @param resources
	 */
	public void addLinks(CollectionModel<EntityModel<T>> resources) {
		resources.add(getCollectionLinkBuilder().withSelfRel());
	}

	/**
	 * Build up a URI for the collection using the Spring web controller followed by the resource type transformed by the
	 * {@link LinkRelationProvider}. Assumption is that an {@literal EmployeeController} serving up {@literal Employee}
	 * objects will be serving resources at {@code /employees} and {@code /employees/1}. If this is not the case, simply
	 * override this method in your concrete instance, or resort to overriding {@link #addLinks(EntityModel)} and
	 * {@link #addLinks(CollectionModel)} where you have full control over exactly what links are put in the individual
	 * and collection resources.
	 *
	 * @return
	 */
	protected LinkBuilder getCollectionLinkBuilder() {

		WebMvcLinkBuilder linkBuilder = linkTo(this.controllerClass);

		for (String pathComponent : (getPrefix() + this.relProvider.getCollectionResourceRelFor(this.resourceType))
				.split("/")) {
			if (!pathComponent.isEmpty()) {
				linkBuilder = linkBuilder.slash(pathComponent);
			}
		}

		return linkBuilder;
	}

	/**
	 * Provide opportunity to override the base path for the URI.
	 */
	private String getPrefix() {
		return getBasePath().isEmpty() ? "" : getBasePath() + "/";
	}

	public Class<?> getControllerClass() {
		return controllerClass;
	}

	public LinkRelationProvider getRelProvider() {
		return relProvider;
	}

	public Class<?> getResourceType() {
		return resourceType;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
}

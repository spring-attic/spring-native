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

package org.springframework.hateoas.config;

import org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration;
import org.springframework.boot.autoconfigure.hateoas.HypermediaHttpMessageConverterConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonHints;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.FieldHint;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;


@NativeHint(trigger = WebStackImportSelector.class, types = {
		@TypeHint(types = {
				WebMvcHateoasConfiguration.class,
				WebFluxHateoasConfiguration.class
		})
})
@NativeHint(trigger = HypermediaConfigurationImportSelector.class, types =
@TypeHint(types = {
		HypermediaConfigurationImportSelector.class,
		EnableHypermediaSupport.class,
		HypermediaType.class,
		HypermediaType[].class,
		MediaTypeConfigurationProvider.class
}))
@NativeHint(trigger = HypermediaAutoConfiguration.class,
		types = {
				@TypeHint(types = {

						HypermediaConfigurationImportSelector.class,
						HateoasConfiguration.class,
						HypermediaHttpMessageConverterConfiguration.class,

						Relation.class,

						Link.class,
						LinkRelation.class,
						Affordance.class,
						AffordanceModel.class,

						EntityModel.class,
						PagedModel.class,
						RepresentationModel.class,

						RestTemplateHateoasConfiguration.class,
				},
						typeNames = {

								"org.springframework.hateoas.StringLinkRelation",
								"org.springframework.hateoas.EntityModel$MapSuppressingUnwrappingSerializer",

								// HAL
								"org.springframework.hateoas.mediatype.hal.HalConfiguration",
								"org.springframework.hateoas.mediatype.hal.Jackson2HalModule",
								"org.springframework.hateoas.mediatype.hal.LinkMixin",
								"org.springframework.hateoas.mediatype.hal.RepresentationModelMixin",
								"org.springframework.hateoas.mediatype.hal.CollectionModelMixin",
								"org.springframework.hateoas.mediatype.hal.HalLinkRelation",
								"org.springframework.hateoas.mediatype.hal.Jackson2HalModule$HalLink",
								"org.springframework.hateoas.mediatype.hal.Jackson2HalModule$HalLinkListSerializer",
								"org.springframework.hateoas.mediatype.hal.Jackson2HalModule$HalLinkListDeserializer",
								"org.springframework.hateoas.mediatype.hal.Jackson2HalModule$TrueOnlyBooleanSerializer",
								"org.springframework.hateoas.mediatype.hal.Jackson2HalModule$EmbeddedMapper",
								"org.springframework.hateoas.mediatype.hal.forms.HalFormsMediaTypeConfiguration"

								// TODO: ALPS

								// TODO: COLLECTION JSON

								// TODO: UBER

						}, access = AccessBits.ALL
				),
				@TypeHint(
						types = CollectionModel.class,
						fields = @FieldHint(name = "content", allowUnsafeAccess = true, allowWrite = true)
				)
		},
		imports = {
				JacksonHints.class
		}
)
public class HateoasHints implements NativeConfiguration {

}

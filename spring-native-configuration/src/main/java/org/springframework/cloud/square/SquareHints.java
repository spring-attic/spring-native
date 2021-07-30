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

package org.springframework.cloud.square;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfiguration;
import org.springframework.cloud.square.retrofit.DefaultRetrofitClientConfiguration;
import org.springframework.cloud.square.retrofit.RetrofitAutoConfiguration;
import org.springframework.cloud.square.retrofit.RetrofitClientFactoryBean;
import org.springframework.cloud.square.retrofit.core.AbstractRetrofitClientFactoryBean;
import org.springframework.cloud.square.retrofit.core.RetrofitClientSpecification;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

import retrofit2.Retrofit;

/**
 * Provides hints for commonly required dependencies when using Spring Cloud Square Retrofit.
 *
 * @author Josh Long
 * @author Andy Clement
 */
@NativeHint(
	trigger = RetrofitAutoConfiguration.class,
	options = "-H:+AddAllCharsets --enable-url-protocols=http,https",
	types = {
		@TypeHint(
			access = AccessBits.ALL,
			types = {
				Retrofit.Builder.class,
				AbstractRetrofitClientFactoryBean.class,
				RetrofitClientFactoryBean.class,
				RetrofitClientSpecification.class,
				DefaultRetrofitClientConfiguration.class,
				LoadBalancerClientConfiguration.class,
			})
	}
)
public class SquareHints implements NativeConfiguration {
}


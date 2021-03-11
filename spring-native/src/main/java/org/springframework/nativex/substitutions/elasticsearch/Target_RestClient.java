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

package org.springframework.nativex.substitutions.elasticsearch;

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.auth.AuthScheme;
import org.apache.http.client.AuthCache;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.UnsupportedSchemeException;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.util.Args;
import org.elasticsearch.client.Node;

import org.springframework.nativex.substitutions.OnlyIfPresent;

/**
 * Substitute the {@link org.apache.http.impl.client.BasicAuthCache} (causing trouble by using an {@link ObjectOutputStream}
 * for serialization), with a {@link Map} based implementation.
 */
@TargetClass(className = "org.elasticsearch.client.RestClient", onlyWith = OnlyIfPresent.class)
final class Target_RestClient {

	@Alias
	private ConcurrentMap<HttpHost, DeadHostState> blacklist;

	@Alias
	private volatile NodeTuple<List<Node>> nodeTuple;

	@Substitute
	public synchronized void setNodes(Collection<Node> nodes) {

		if (nodes == null || nodes.isEmpty()) {
			throw new IllegalArgumentException("nodes must not be null or empty");
		}

		AuthCache authCache = new MapAuthCache();

		Map<HttpHost, Node> nodesByHost = new LinkedHashMap<>();
		for (Node node : nodes) {

			Objects.requireNonNull(node, "node cannot be null");
			nodesByHost.put(node.getHost(), node);
			authCache.put(node.getHost(), new BasicScheme());
		}

		this.nodeTuple = new NodeTuple<>(Collections.unmodifiableList(new ArrayList<>(nodesByHost.values())),
				authCache);
		this.blacklist.clear();
	}

	@TargetClass(className = "org.elasticsearch.client.DeadHostState", onlyWith = OnlyIfPresent.class)
	final static class DeadHostState {

	}

	@TargetClass(className = "org.elasticsearch.client.RestClient", innerClass = "NodeTuple", onlyWith = OnlyIfPresent.class)
	final static class NodeTuple<T> {

		@Alias
		NodeTuple(final T nodes, final AuthCache authCache) {
		}
	}

	@Contract(threading = ThreadingBehavior.SAFE)
	private static final class MapAuthCache implements AuthCache {

		private final Map<HttpHost, AuthScheme> map;
		private final SchemePortResolver schemePortResolver;

		MapAuthCache() {
			this(DefaultSchemePortResolver.INSTANCE);
		}

		MapAuthCache(SchemePortResolver schemePortResolver) {

			this.map = new ConcurrentHashMap<>();
			this.schemePortResolver = schemePortResolver;
		}

		@Override
		public void put(final HttpHost host, final AuthScheme authScheme) {

			Args.notNull(host, "HTTP host");
			if (authScheme == null) {
				return;
			}

			map.put(getKey(host), authScheme);
		}

		@Override
		public AuthScheme get(final HttpHost host) {

			Args.notNull(host, "HTTP host");
			return map.get(getKey(host));
		}

		@Override
		public void remove(final HttpHost host) {

			Args.notNull(host, "HTTP host");
			map.remove(getKey(host));
		}

		@Override
		public void clear() {
			map.clear();
		}

		@Override
		public String toString() {
			return map.toString();
		}

		private HttpHost getKey(final HttpHost host) {

			if (host.getPort() <= 0) {
				final int port;
				try {
					port = schemePortResolver.resolve(host);
				} catch (final UnsupportedSchemeException ignore) {
					return host;
				}
				return new HttpHost(host.getHostName(), port, host.getSchemeName());
			} else {
				return host;
			}
		}
	}
}

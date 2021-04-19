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

package org.springframework.boot.autoconfigure.session;

import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration.ReactiveSessionConfigurationImportSelector;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration.ServletSessionConfigurationImportSelector;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration.SessionConfigurationImportSelector;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.SerializationHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;

import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;

@NativeHint(trigger = ReactiveSessionConfigurationImportSelector.class, types =
	@TypeHint(types = {
			RedisReactiveSessionConfiguration.class,
			MongoReactiveSessionConfiguration.class,
			NoOpReactiveSessionConfiguration.class})
, abortIfTypesMissing = true)
@NativeHint(trigger = SessionConfigurationImportSelector.class, types =
	@TypeHint(types = {
			RedisSessionConfiguration.class,
			RedisReactiveSessionConfiguration.class,
			MongoSessionConfiguration.class,
			MongoReactiveSessionConfiguration.class,
			JdbcSessionConfiguration.class,
			HazelcastSessionConfiguration.class,
			NoOpSessionConfiguration.class,
			NoOpReactiveSessionConfiguration.class
	})
, abortIfTypesMissing = true, follow = true)
@NativeHint(trigger=ServletSessionConfigurationImportSelector.class, types = {
	@TypeHint(types= {
			RedisSessionConfiguration.class,
			MongoSessionConfiguration.class,
			JdbcSessionConfiguration.class,
			HazelcastSessionConfiguration.class,
			NoOpSessionConfiguration.class
	})
}, abortIfTypesMissing = true)
@NativeHint(trigger = JdbcSessionConfiguration.class,
		resources = @ResourceHint(patterns = {
				"org/springframework/session/jdbc/schema-db2.sql",
				"org/springframework/session/jdbc/schema-derby.sql",
				"org/springframework/session/jdbc/schema-drop-db2.sql",
				"org/springframework/session/jdbc/schema-drop-derby.sql",
				"org/springframework/session/jdbc/schema-drop-h2.sql",
				"org/springframework/session/jdbc/schema-drop-hsqldb.sql",
				"org/springframework/session/jdbc/schema-drop-mysql.sql",
				"org/springframework/session/jdbc/schema-drop-oracle.sql",
				"org/springframework/session/jdbc/schema-drop-postgresql.sql",
				"org/springframework/session/jdbc/schema-drop-sqlite.sql",
				"org/springframework/session/jdbc/schema-drop-sqlserver.sql",
				"org/springframework/session/jdbc/schema-drop-sybase.sql",
				"org/springframework/session/jdbc/schema-h2.sql",
				"org/springframework/session/jdbc/schema-hsqldb.sql",
				"org/springframework/session/jdbc/schema-mysql.sql",
				"org/springframework/session/jdbc/schema-oracle.sql",
				"org/springframework/session/jdbc/schema-postgresql.sql",
				"org/springframework/session/jdbc/schema-sqlite.sql",
				"org/springframework/session/jdbc/schema-sqlserver.sql",
				"org/springframework/session/jdbc/schema-sybase.sql"
		})
)
@NativeHint(trigger = SpringHttpSessionConfiguration.class, serializables = {
	@SerializationHint(types= {
			ArrayList.class,
			TreeMap.class,
			TreeSet.class,
			Locale.class,
			DefaultSavedRequest.class,
			DefaultCsrfToken.class,
			SecurityContextImpl.class,
			SimpleGrantedAuthority.class,
			WebAuthenticationDetails.class,
			User.class,
			AbstractAuthenticationToken.class,
			UsernamePasswordAuthenticationToken.class,
			OAuth2AuthenticationToken.class,
			OAuth2LoginAuthenticationToken.class,
			OAuth2AuthorizationCodeAuthenticationToken.class
	},
	typeNames = {
			"org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken",
			"org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken",
			"java.util.Collections$UnmodifiableCollection",
			"java.util.Collections$UnmodifiableList",
			"java.util.Collections$UnmodifiableRandomAccessList",
			"java.util.Collections$UnmodifiableSet",
			"java.lang.String$CaseInsensitiveComparator",
			"org.springframework.security.core.userdetails.User$AuthorityComparator"
	})
}, abortIfTypesMissing = true)
public class SessionHints implements NativeConfiguration {
}

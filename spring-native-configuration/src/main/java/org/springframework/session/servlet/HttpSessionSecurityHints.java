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

package org.springframework.session.servlet;

import org.springframework.nativex.AotOptions;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.SerializationHint;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.SavedCookie;
import org.springframework.session.CommonSessionSecuritySerializables;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;
import org.springframework.util.ClassUtils;

import java.util.Locale;
import java.util.TreeMap;

@NativeHint(trigger = SpringHttpSessionConfiguration.class,
        imports = CommonSessionSecuritySerializables.class,
        serializables = @SerializationHint(types = {
                TreeMap.class,
                Locale.class,
                DefaultSavedRequest.class,
                DefaultCsrfToken.class,
                WebAuthenticationDetails.class,
                SavedCookie.class
        }, typeNames = "java.lang.String$CaseInsensitiveComparator")
        , abortIfTypesMissing = true)
public class HttpSessionSecurityHints implements NativeConfiguration {

    @Override
    public boolean isValid(AotOptions aotOptions) {
        return ClassUtils.isPresent("javax.servlet.http.HttpSession", null)
                && ClassUtils.isPresent("org.springframework.security.web.csrf.DefaultCsrfToken", null);
    }
}

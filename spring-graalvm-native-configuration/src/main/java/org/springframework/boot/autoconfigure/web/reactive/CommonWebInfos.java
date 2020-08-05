/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.boot.autoconfigure.web.reactive;

import org.springframework.graalvm.extension.ResourcesInfo;

// Accessed from org.apache.catalina.startup.Tomcat
@ResourcesInfo(patterns= {
	"org/springframework/http/mime.types",
	"org/apache/catalina/startup/MimeTypeMappings.properties",
	"static/.*",
	"templates/.*",
	"META-INF/resources/webjars/.*"
})
//TODO deletable once confirmed tomcat version will contain these from now on (also see WebFluxHints)
@ResourcesInfo(patterns= {"javax.servlet.LocalStrings","javax.servlet.http.LocalStrings"},isBundle=true)
public class CommonWebInfos {

}

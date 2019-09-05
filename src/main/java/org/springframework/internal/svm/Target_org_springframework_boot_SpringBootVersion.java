/*
 * Copyright 2019 Contributors
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
package org.springframework.internal.svm;

import org.springframework.beans.BeansException;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * 
 * @author Andy Clement
 */
@TargetClass(className="org.springframework.boot.SpringBootVersion", onlyWith = OnlyPresent.class)
public final class Target_org_springframework_boot_SpringBootVersion {

	@Substitute
	public static String getVersion() throws BeansException {
		return null;
		// Without this, line 66 in SpringBootVersion 2.2.0 snapshots (>m2) fails with NPE:
		
		// URL codeSourceLocation = SpringBootVersion.class.getProtectionDomain()
		// .getCodeSource().getLocation(); // this is line 66

		//java.lang.NullPointerException
		//	at org.springframework.boot.SpringBootVersion.determineSpringBootVersion(SpringBootVersion.java:66)
		//	at org.springframework.boot.SpringBootVersion.getVersion(SpringBootVersion.java:56)
		//	at org.springframework.boot.SpringBootBanner.printBanner(SpringBootBanner.java:51)
		//	at org.springframework.boot.SpringApplicationBannerPrinter.print(SpringApplicationBannerPrinter.java:71)
		//	at org.springframework.boot.SpringApplication.printBanner(SpringApplication.java:582)
		//	at org.springframework.boot.SpringApplication.run(SpringApplication.java:312)
		//	at com.example.func.BuncApplication.run(BuncApplication.java:55)
		//	at com.example.func.BuncApplication.main(BuncApplication.java:34)
	}
}
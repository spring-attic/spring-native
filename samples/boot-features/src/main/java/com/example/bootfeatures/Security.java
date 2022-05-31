/*
 * Copyright 2019-2022 the original author or authors.
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

package com.example.bootfeatures;

import java.util.List;

import org.springframework.boot.context.properties.bind.DefaultValue;

public class Security {

	private final String username;

	private final String password;

	private final List<String> roles;

	public Security(String username, String password,
			@DefaultValue("USER") List<String> roles) {
		this.username = username;
		this.password = password;
		this.roles = roles;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public List<String> getRoles() {
		return roles;
	}
    
    @Override
    public String toString() {
        return username+" "+password+" "+roles;
    }
}
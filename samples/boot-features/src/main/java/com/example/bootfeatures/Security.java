package com.example.bootfeatures;

import java.util.List;

import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConstructorBinding
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
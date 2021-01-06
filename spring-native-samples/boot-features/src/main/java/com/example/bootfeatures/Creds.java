package com.example.bootfeatures;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "credentials")
@ConstructorBinding
public class Creds {
 
    private final String username;
    private final String password;
 
    public Creds(String username, String password) {
        this.username = username;
        this.password = password;
    }
 
    public String getUsername() {
        return username;
    }
 
    public String getPassword() {
        return password;
    }
}
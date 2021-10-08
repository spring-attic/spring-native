package com.example.ldapsecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.TypeHint;

@SpringBootApplication
public class SecurityLdapApplication {

	public static void main(String[] args) throws Throwable {
		SpringApplication.run(SecurityLdapApplication.class, args);
	}

}

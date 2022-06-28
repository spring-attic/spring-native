package com.example.securingweb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
                .authorizeRequests(authorize -> authorize
                        .mvcMatchers("/rest/anonymous").permitAll()
                        .mvcMatchers("/rest/admin").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic()
                .and()
				.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user =
				User.withDefaultPasswordEncoder()
						.username("user")
						.password("password")
						.roles("USER")
						.build();
		UserDetails admin =
				User.withDefaultPasswordEncoder()
						.username("admin")
						.password("password")
						.roles("ADMIN")
						.build();

		return new InMemoryUserDetailsManager(user, admin);
	}
}

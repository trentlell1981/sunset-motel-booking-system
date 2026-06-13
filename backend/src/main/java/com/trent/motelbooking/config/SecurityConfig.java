package com.trent.motelbooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(Customizer.withDefaults()).csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/rooms/**").permitAll()
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/rooms/**").authenticated()
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/rooms").authenticated()
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/bookings").permitAll()
						.requestMatchers("/api/bookings/**").authenticated().anyRequest().permitAll())
				.httpBasic(Customizer.withDefaults());

		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails admin = User.withUsername("admin").password("{noop}admin123").roles("ADMIN").build();

		return new InMemoryUserDetailsManager(admin);
	}
}
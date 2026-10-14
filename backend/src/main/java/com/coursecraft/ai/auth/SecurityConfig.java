package com.coursecraft.ai.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	private final boolean jwtEnabled;

	public SecurityConfig(@Value("${app.security.jwt.enabled:false}") boolean jwtEnabled) {
		this.jwtEnabled = jwtEnabled;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(authorize -> {
					authorize
							.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
							.requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll();

					if (jwtEnabled) {
						authorize.requestMatchers("/api/**").authenticated();
					} else {
						authorize.requestMatchers("/api/**").permitAll();
					}

					authorize.anyRequest().denyAll();
				});

		if (jwtEnabled) {
			http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
		}

		return http.build();
	}
}

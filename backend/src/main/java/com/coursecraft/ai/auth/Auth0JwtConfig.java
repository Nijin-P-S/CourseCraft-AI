package com.coursecraft.ai.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@ConditionalOnProperty(name = "app.security.jwt.enabled", havingValue = "true")
public class Auth0JwtConfig {

	@Bean
	public JwtDecoder jwtDecoder(
			@Value("${app.security.auth0.issuer-uri}") String issuerUri,
			@Value("${app.security.auth0.audience}") String audience
	) {
		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
		OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
		OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
			if (jwt.getAudience().contains(audience)) {
				return OAuth2TokenValidatorResult.success();
			}

			OAuth2Error error = new OAuth2Error(
					"invalid_token",
					"The required audience is missing",
					null
			);
			return OAuth2TokenValidatorResult.failure(error);
		};

		jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));
		return jwtDecoder;
	}
}

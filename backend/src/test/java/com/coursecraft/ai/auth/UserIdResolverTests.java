package com.coursecraft.ai.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

class UserIdResolverTests {

	@Test
	void returnsLocalDevUserWhenJwtIsDisabledAndNoAuthenticationExists() {
		UserIdResolver resolver = new UserIdResolver(false, "local-test-user");

		assertThat(resolver.resolve(null)).isEqualTo("local-test-user");
	}

	@Test
	void returnsAuthenticatedPrincipalName() {
		UserIdResolver resolver = new UserIdResolver(true, "local-test-user");
		UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
				"auth0|user-123",
				"token",
				List.of()
		);

		assertThat(resolver.resolve(authentication)).isEqualTo("auth0|user-123");
	}

	@Test
	void rejectsMissingAuthenticationWhenJwtIsEnabled() {
		UserIdResolver resolver = new UserIdResolver(true, "local-test-user");

		assertThatThrownBy(() -> resolver.resolve(null))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Authentication is required");
	}
}

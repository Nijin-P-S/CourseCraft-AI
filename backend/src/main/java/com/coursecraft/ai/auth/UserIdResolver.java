package com.coursecraft.ai.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UserIdResolver {

	private final boolean jwtEnabled;
	private final String localDevUserId;

	public UserIdResolver(
			@Value("${app.security.jwt.enabled:false}") boolean jwtEnabled,
			@Value("${app.security.local-dev-user-id:local-dev-user}") String localDevUserId
	) {
		this.jwtEnabled = jwtEnabled;
		this.localDevUserId = localDevUserId;
	}

	public String resolve(Authentication authentication) {
		if (hasAuthenticatedUser(authentication)) {
			return authentication.getName();
		}

		if (!jwtEnabled) {
			return localDevUserId;
		}

		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
	}

	private boolean hasAuthenticatedUser(Authentication authentication) {
		return authentication != null
				&& authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken)
				&& !"anonymousUser".equals(authentication.getName());
	}
}

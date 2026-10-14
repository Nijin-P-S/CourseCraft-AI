package com.coursecraft.ai.auth;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserIdResolver userIdResolver;
	private final boolean jwtEnabled;

	public AuthController(
			UserIdResolver userIdResolver,
			@Value("${app.security.jwt.enabled:false}") boolean jwtEnabled
	) {
		this.userIdResolver = userIdResolver;
		this.jwtEnabled = jwtEnabled;
	}

	@GetMapping("/me")
	public ResponseEntity<AuthUserResponse> getCurrentUser(Authentication authentication) {
		String userId = userIdResolver.resolve(authentication);
		List<String> authorities = authentication == null
				? List.of()
				: authentication.getAuthorities().stream()
						.map(GrantedAuthority::getAuthority)
						.toList();

		return ResponseEntity.ok(new AuthUserResponse(
				userId,
				jwtEnabled ? "jwt" : "local-dev",
				authorities
		));
	}
}

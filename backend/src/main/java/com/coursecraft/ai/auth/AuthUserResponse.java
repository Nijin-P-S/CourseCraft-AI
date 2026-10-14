package com.coursecraft.ai.auth;

import java.util.List;

public record AuthUserResponse(
		String userId,
		String authMode,
		List<String> authorities
) {
}

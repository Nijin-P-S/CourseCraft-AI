package com.coursecraft.ai.youtube.controller;

import com.coursecraft.ai.auth.UserIdResolver;
import com.coursecraft.ai.youtube.dto.YouTubeVideoResponse;
import com.coursecraft.ai.youtube.service.YouTubeSearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
@Validated
public class YouTubeController {

	private final YouTubeSearchService youTubeSearchService;
	private final UserIdResolver userIdResolver;

	@GetMapping
	public ResponseEntity<YouTubeVideoResponse> searchVideo(
			@NotBlank @RequestParam String query,
			Authentication authentication
	) {
		userIdResolver.resolve(authentication);
		return ResponseEntity.ok(youTubeSearchService.searchFirstVideo(query));
	}
}

package com.coursecraft.ai.lesson.controller;

import com.coursecraft.ai.lesson.dto.LessonContentResponse;
import com.coursecraft.ai.lesson.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/courses/{courseId}/modules/{moduleId}/lessons")
@RequiredArgsConstructor
public class LessonController {

	private final LessonService lessonService;

	@GetMapping("/{lessonId}")
	public ResponseEntity<LessonContentResponse> getLessonContent(
			@PathVariable String courseId,
			@PathVariable String moduleId,
			@PathVariable String lessonId,
			Authentication authentication
	) {
		String userId = requireUserId(authentication);
		LessonContentResponse response = lessonService.getLessonContent(
				courseId,
				moduleId,
				lessonId,
				userId
		);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/{lessonId}/generate")
	public ResponseEntity<LessonContentResponse> generateLessonContent(
			@PathVariable String courseId,
			@PathVariable String moduleId,
			@PathVariable String lessonId,
			Authentication authentication
	) {
		String userId = requireUserId(authentication);
		LessonContentResponse response = lessonService.generateLessonContent(
				courseId,
				moduleId,
				lessonId,
				userId
		);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	private String requireUserId(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
		}

		return authentication.getName();
	}
}

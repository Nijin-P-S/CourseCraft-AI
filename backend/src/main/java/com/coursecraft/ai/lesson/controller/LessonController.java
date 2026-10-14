package com.coursecraft.ai.lesson.controller;

import com.coursecraft.ai.lesson.dto.LessonContentResponse;
import com.coursecraft.ai.lesson.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

	@PostMapping("/{lessonId}/generate")
	public ResponseEntity<LessonContentResponse> generateLessonContent(
			@PathVariable String courseId,
			@PathVariable String moduleId,
			@PathVariable String lessonId,
			Authentication authentication
	) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
		}

		LessonContentResponse response = lessonService.generateLessonContent(
				courseId,
				moduleId,
				lessonId,
				authentication.getName()
		);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}

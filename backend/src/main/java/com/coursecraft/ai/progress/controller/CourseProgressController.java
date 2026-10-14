package com.coursecraft.ai.progress.controller;

import com.coursecraft.ai.progress.dto.CourseProgressResponse;
import com.coursecraft.ai.progress.dto.UpdateLessonProgressRequest;
import com.coursecraft.ai.progress.service.CourseProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/courses/{courseId}/progress")
@RequiredArgsConstructor
public class CourseProgressController {

	private final CourseProgressService courseProgressService;

	@GetMapping
	public ResponseEntity<CourseProgressResponse> getProgress(
			@PathVariable String courseId,
			Authentication authentication
	) {
		String userId = requireUserId(authentication);
		return ResponseEntity.ok(courseProgressService.getProgress(courseId, userId));
	}

	@PutMapping("/lessons/{lessonId}")
	public ResponseEntity<CourseProgressResponse> updateLessonProgress(
			@PathVariable String courseId,
			@PathVariable String lessonId,
			@Valid @RequestBody UpdateLessonProgressRequest request,
			Authentication authentication
	) {
		String userId = requireUserId(authentication);
		CourseProgressResponse response = courseProgressService.updateLessonProgress(
				courseId,
				lessonId,
				userId,
				request
		);

		return ResponseEntity.ok(response);
	}

	private String requireUserId(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
		}

		return authentication.getName();
	}
}

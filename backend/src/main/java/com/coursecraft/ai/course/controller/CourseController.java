package com.coursecraft.ai.course.controller;

import com.coursecraft.ai.auth.UserIdResolver;
import com.coursecraft.ai.course.dto.CourseResponse;
import com.coursecraft.ai.course.dto.GenerateCourseRequest;
import com.coursecraft.ai.course.service.CourseService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

	private final CourseService courseService;
	private final UserIdResolver userIdResolver;

	@GetMapping
	public ResponseEntity<List<CourseResponse>> getCourses(Authentication authentication) {
		String userId = userIdResolver.resolve(authentication);
		return ResponseEntity.ok(courseService.getCoursesForUser(userId));
	}

	@GetMapping("/{courseId}")
	public ResponseEntity<CourseResponse> getCourse(
			@PathVariable String courseId,
			Authentication authentication
	) {
		String userId = userIdResolver.resolve(authentication);
		return ResponseEntity.ok(courseService.getCourse(courseId, userId));
	}

	@PostMapping("/generate")
	public ResponseEntity<CourseResponse> generateCourse(
			@Valid @RequestBody GenerateCourseRequest request,
			Authentication authentication
	) {
		String userId = userIdResolver.resolve(authentication);
		CourseResponse response = courseService.generateCourse(request, userId);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}

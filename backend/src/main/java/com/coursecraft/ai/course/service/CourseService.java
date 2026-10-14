package com.coursecraft.ai.course.service;

import com.coursecraft.ai.course.dto.CourseResponse;
import com.coursecraft.ai.course.dto.GenerateCourseRequest;
import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.repository.CourseRepository;
import com.coursecraft.ai.generation.CourseOutlineGenerator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

@Service
@Validated
@RequiredArgsConstructor
public class CourseService {

	private final CourseRepository courseRepository;
	private final CourseOutlineGenerator courseOutlineGenerator;

	public CourseResponse generateCourse(GenerateCourseRequest request, String creatorId) {
		Course generatedCourse = courseOutlineGenerator.generate(request.topic(), creatorId);
		Course savedCourse = courseRepository.save(generatedCourse);

		return CourseResponse.from(savedCourse);
	}

	public List<CourseResponse> getCoursesForUser(String userId) {
		return courseRepository.findByCreatorIdOrderByCreatedAtDesc(userId).stream()
				.map(CourseResponse::from)
				.toList();
	}

	public CourseResponse getCourse(String courseId, String userId) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

		if (!course.getCreatorId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course does not belong to the authenticated user");
		}

		return CourseResponse.from(course);
	}
}

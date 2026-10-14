package com.coursecraft.ai.course.service;

import com.coursecraft.ai.course.dto.CourseResponse;
import com.coursecraft.ai.course.dto.GenerateCourseRequest;
import com.coursecraft.ai.course.model.Course;
import com.coursecraft.ai.course.repository.CourseRepository;
import com.coursecraft.ai.generation.CourseOutlineGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

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
}

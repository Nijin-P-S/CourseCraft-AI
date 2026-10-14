package com.coursecraft.ai.course.dto;

import com.coursecraft.ai.course.model.Course;
import java.time.Instant;
import java.util.List;

public record CourseResponse(
		String id,
		String title,
		String description,
		String creatorId,
		List<String> tags,
		List<CourseModuleResponse> modules,
		Instant createdAt,
		Instant updatedAt
) {
	public static CourseResponse from(Course course) {
		return new CourseResponse(
				course.getId(),
				course.getTitle(),
				course.getDescription(),
				course.getCreatorId(),
				course.getTags(),
				course.getModules().stream()
						.map(CourseModuleResponse::from)
						.toList(),
				course.getCreatedAt(),
				course.getUpdatedAt()
		);
	}
}

package com.coursecraft.ai.course.dto;

import com.coursecraft.ai.course.model.CourseModule;
import java.util.List;

public record CourseModuleResponse(
		String id,
		String title,
		String description,
		int orderIndex,
		List<LessonSummaryResponse> lessons
) {
	public static CourseModuleResponse from(CourseModule module) {
		return new CourseModuleResponse(
				module.getId(),
				module.getTitle(),
				module.getDescription(),
				module.getOrderIndex(),
				module.getLessons().stream()
						.map(LessonSummaryResponse::from)
						.toList()
		);
	}
}
